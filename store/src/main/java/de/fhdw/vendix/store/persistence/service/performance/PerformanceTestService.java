package de.fhdw.vendix.store.persistence.service.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Startet JMeter-Tests per CLI (kein GUI nötig).
 *
 * Strategie: Die Master-JMX enthält alle 5 Thread Groups.
 * Vor jedem Start wird eine temporäre JMX erzeugt, in der
 * nur die gewählte Thread Group aktiviert ist – die anderen
 * bleiben deaktiviert. So bleibt die Master-JMX unberührt.
 */
@Service
public class PerformanceTestService {

    private static final Logger log = LoggerFactory.getLogger(PerformanceTestService.class);
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final PerformanceTestProperties props;

    // --- Zustandsverwaltung ---
    private Process       currentProcess;
    private PerformanceTestType runningTest;
    private LocalDateTime       startTime;
    private Integer             lastExitCode;
    private PerformanceTestType lastFinishedTest;
    private String              lastResultFile;

    public PerformanceTestService(PerformanceTestProperties props) {
        this.props = props;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Startet den gewählten Testtyp.
     * Wirft {@link IllegalStateException}, wenn bereits ein Test läuft.
     * Wirft {@link IllegalArgumentException}, wenn masterJmx nicht konfiguriert ist.
     */
    public synchronized void startTest(PerformanceTestType type) throws IOException {
        if (isRunning()) {
            throw new IllegalStateException(
                    "Es läuft bereits ein Test: " + runningTest.getLabel() +
                            " (gestartet: " + startTime + ")");
        }

        String masterJmx = props.getMasterJmx();
        if (masterJmx == null || masterJmx.isBlank()) {
            throw new IllegalArgumentException(
                    "vendix.performance.master-jmx ist nicht gesetzt. " +
                            "Bitte in application-dev.properties konfigurieren.");
        }

        // Ergebnis-Verzeichnis anlegen
        File resultsDir = new File(props.getResultsDir());
        if (!resultsDir.exists()) {
            Files.createDirectories(resultsDir.toPath());
        }

        // Temp-JMX mit nur einer aktiven Thread Group erzeugen
        Path tempJmx = buildTempJmx(type, new File(masterJmx));

        // Ergebnis-Datei
        String timestamp   = LocalDateTime.now().format(TIMESTAMP_FMT);
        String resultFile  = props.getResultsDir() + File.separator +
                type.name().toLowerCase() + "_" + timestamp + ".csv";

        // JMeter-Prozess starten
        // Output direkt in eine Log-Datei umleiten (NICHT in einen Pipe-Puffer),
        // damit der Prozess nicht wegen vollem Buffer hängt.
        File jmeterLog = new File(props.getResultsDir() + File.separator +
                "jmeter_" + type.name().toLowerCase() + "_" + timestamp + ".log");

        ProcessBuilder pb = new ProcessBuilder(
                props.getJmeterBin(),
                "-n",                        // Non-GUI-Modus
                "-t", tempJmx.toString(),    // Testplan
                "-l", resultFile,            // Ergebnis-CSV
                "-e",                        // HTML-Report erzeugen
                "-o", props.getResultsDir() + File.separator + "report_" +
                type.name().toLowerCase() + "_" + timestamp
        );
        pb.redirectErrorStream(true);
        pb.redirectOutput(jmeterLog);        // stdout+stderr → Datei, kein hängender Puffer

        log.info("Starte JMeter-Test '{}': {}", type.getLabel(), pb.command());
        log.info("JMeter-Log: {}", jmeterLog.getAbsolutePath());
        currentProcess   = pb.start();
        runningTest      = type;
        startTime        = LocalDateTime.now();
        lastResultFile   = resultFile;

        // Hintergrund-Thread, der Exit-Code einsammelt und loggt
        Thread watcher = new Thread(() -> {
            try {
                int code = currentProcess.waitFor();
                synchronized (PerformanceTestService.this) {
                    lastExitCode     = code;
                    lastFinishedTest = runningTest;
                    runningTest      = null;
                    if (code == 0) {
                        log.info("JMeter-Test '{}' erfolgreich beendet. CSV: {}",
                                lastFinishedTest.getLabel(), resultFile);
                    } else {
                        log.error("JMeter-Test '{}' mit Exit-Code {} beendet. Details: {}",
                                lastFinishedTest.getLabel(), code, jmeterLog.getAbsolutePath());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        watcher.setDaemon(true);
        watcher.start();
    }

    /** Bricht den laufenden Test ab (SIGTERM). */
    public synchronized void stopTest() {
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroy();
            log.info("JMeter-Test '{}' wurde abgebrochen.", runningTest);
        }
    }

    public synchronized boolean isRunning() {
        return currentProcess != null && currentProcess.isAlive();
    }

    public synchronized PerformanceTestType getRunningTest()      { return runningTest; }
    public synchronized LocalDateTime       getStartTime()        { return startTime; }
    public synchronized Integer             getLastExitCode()     { return lastExitCode; }
    public synchronized PerformanceTestType getLastFinishedTest() { return lastFinishedTest; }
    public synchronized String              getLastResultFile()   { return lastResultFile; }

    /** Lesbare Status-Zusammenfassung für die UI. */
    public synchronized String getStatusSummary() {
        if (isRunning()) {
            return "▶ Läuft: " + runningTest.getLabel() +
                    " (seit " + startTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ")";
        }
        if (lastFinishedTest != null) {
            String result = (lastExitCode != null && lastExitCode == 0) ? "✔ Erfolgreich" : "✘ Fehler";
            return result + ": " + lastFinishedTest.getLabel() + " (Exit-Code " + lastExitCode + ")";
        }
        return "Kein Test gestartet.";
    }

    // -------------------------------------------------------------------------
    // Intern: Temp-JMX bauen
    // -------------------------------------------------------------------------

    /**
     * Liest die Master-JMX, aktiviert nur die ThreadGroup, deren testname
     * mit dem JMX-Prefix des gewählten Typs beginnt, und schreibt das Ergebnis
     * in eine temporäre Datei.
     */
    private Path buildTempJmx(PerformanceTestType type, File masterJmx) throws IOException {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(masterJmx);

            // Alle ThreadGroup-Elemente suchen (normaler ThreadGroup + bzgl. Stresstest etc.)
            String[] groupTags = {"ThreadGroup", "SetupThreadGroup", "PostThreadGroup"};
            for (String tag : groupTags) {
                NodeList groups = doc.getElementsByTagName(tag);
                for (int i = 0; i < groups.getLength(); i++) {
                    Element el = (Element) groups.item(i);
                    // SetupThreadGroup immer aktiviert lassen (holt Token + registriert Kasse)
                    if ("SetupThreadGroup".equals(tag)) {
                        el.setAttribute("enabled", "true");
                        continue;
                    }
                    String testname = el.getAttribute("testname");
                    boolean shouldEnable = testname.startsWith(type.getJmxPrefix());
                    el.setAttribute("enabled", String.valueOf(shouldEnable));
                }
            }

            // In temp-Datei schreiben
            Path tempFile = Files.createTempFile("vendix-jmeter-" + type.name().toLowerCase() + "-", ".jmx");
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(tempFile.toFile()));

            log.debug("Temp-JMX erstellt: {}", tempFile);
            return tempFile;

        } catch (Exception e) {
            throw new IOException("Fehler beim Verarbeiten der Master-JMX: " + e.getMessage(), e);
        }
    }
}