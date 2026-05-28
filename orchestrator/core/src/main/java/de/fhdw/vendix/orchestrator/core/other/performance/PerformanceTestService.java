package de.fhdw.vendix.orchestrator.core.other.performance;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Startet und stoppt k6-Lasttests via Docker.
 *
 * <p>Features:
 * <ul>
 *   <li>k6 läuft im Container {@code vendix-k6}</li>
 *   <li>Metriken werden live nach Prometheus gepusht ({@code experimental-prometheus-rw})</li>
 *   <li>Am Ende jedes Tests wird automatisch ein HTML-Report gespeichert
 *       ({@code K6_WEB_DASHBOARD_EXPORT}) — sichtbar im k6 Live Dashboard und als Datei</li>
 *   <li>Das auszuführende k6-Skript wird per {@link TestType#getScript()} bestimmt,
 *       sodass klassische Lasttests ({@code test.js}) und der Messaging-E2E-Test
 *       ({@code messaging-e2e-test.js}) über dieselbe Infrastruktur laufen</li>
 *   <li>Zusätzliche Umgebungsvariablen (z.B. STORE_ID, ORDER_RATE) können pro
 *       Teststart als {@code Map<String,String>} übergeben werden</li>
 * </ul>
 */
@Service
public class PerformanceTestService {

    private static final Logger log      = LoggerFactory.getLogger(PerformanceTestService.class);
    private static final String SEP      = "═".repeat(70);
    private static final String K6_CONTAINER  = "vendix-k6";
    private static final String K6_SCRIPT_DIR = "/etc/k6/scripts/";
    private static final String K6_COMPOSE    = ".docker/testing/docker-compose.yaml";
    private static final String PROMETHEUS_RW = "experimental-prometheus-rw";

    // HTML-Report wird im Container unter diesem Pfad gespeichert.
    // Der Ordner /etc/k6/reports ist im Volume gemountet → nach dem Test
    // findest du die Datei in .docker/testing/k6/reports/ auf dem Host.
    private static final String REPORT_DIR = "/etc/k6/reports";

    private static final DateTimeFormatter REPORT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm").withZone(ZoneId.systemDefault());

    private final String orchestratorBaseUrl;
    private final String storeBaseUrl;
    private final long storeId;
    private final String registerIds;
    private final String cashierIds;
    private final AtomicBoolean                       running    = new AtomicBoolean(false);
    private final AtomicReference<@Nullable Process>     process    = new AtomicReference<>(null);
    private final AtomicReference<@Nullable TestType>    activeTest = new AtomicReference<>(null);
    private final AtomicLong                          startedAt  = new AtomicLong(0);

    public PerformanceTestService(
            @Value("${vendix.loadtests.orchestrator-base-url:http://host.docker.internal:8080}") String orchestratorBaseUrl,
            @Value("${vendix.loadtests.store-base-url:http://host.docker.internal:8081}") String storeBaseUrl,
            @Value("${vendix.loadtests.store-id:1}") long storeId,
            @Value("${vendix.loadtests.register-ids:1,2,3}") String registerIds,
            @Value("${vendix.loadtests.cashier-ids:4,5,6}") String cashierIds
    ) {
        this.orchestratorBaseUrl = orchestratorBaseUrl;
        this.storeBaseUrl = storeBaseUrl;
        this.storeId = storeId;
        this.registerIds = registerIds;
        this.cashierIds = cashierIds;
    }

    // ─── Starten ──────────────────────────────────────────────────────────────

    /**
     * Startet einen k6-Lasttest ohne zusätzliche Umgebungsvariablen.
     * Kurzform von {@link #startTest(TestType, Consumer, Map)}.
     */
    public void startTest(TestType testType, Consumer<String> logConsumer) throws IOException {
        startTest(testType, logConsumer, Map.of());
    }

    /**
     * Startet einen k6-Lasttest mit optionalen zusätzlichen Umgebungsvariablen.
     *
     * <p>Die Variablen werden als {@code -e KEY=VALUE} an {@code docker exec} übergeben
     * und stehen im k6-Skript via {@code __ENV.KEY} zur Verfügung.
     *
     * <p>Typische Zusatzvariablen für den Messaging-E2E-Test:
     * <pre>
     *   STORE_URL       – z.B. http://host.docker.internal:8081 lokal oder http://store:8081 im Docker-Netzwerk
     *   STORE_ID        – z.B. 1
     *   ORDER_RATE      – Orders/Minute, z.B. 30
     *   URGENT_RATIO    – Anteil dringend, z.B. 0.3
     *   VERIFY_STOCK    – Bestandsprüfung, true/false
     *   VERIFY_WAIT_MS  – Wartezeit vor Verifikation in ms, z.B. 2000
     * </pre>
     *
     * @param testType    Testszenario (bestimmt Skript und Standard-Konfiguration)
     * @param logConsumer Callback, der jede Log-Zeile aus k6's stdout empfängt
     * @param extraEnv    Zusätzliche Umgebungsvariablen (können leer sein)
     * @throws IOException            wenn der k6-Container nicht erreichbar ist
     * @throws IllegalStateException  wenn bereits ein Test läuft
     */
    public void startTest(TestType testType, Consumer<String> logConsumer,
                          Map<String, String> extraEnv) throws IOException {
        if (running.get()) {
            @Nullable TestType current = activeTest.get();
            String name = current != null ? current.getDisplayName() : "Unbekannt";
            throw new IllegalStateException("Ein Test läuft bereits: " + name);
        }

        Path repositoryRoot = resolveRepositoryRoot();
        ensureK6ContainerRunning(repositoryRoot, logConsumer);

        // Report-Dateiname: z.B. vendix-messaging-e2e_2026-04-18_14-30.html
        String timestamp  = REPORT_FMT.format(Instant.now());
        String reportFile = REPORT_DIR + "/vendix-" + testType.getScenarioKey()
                + "_" + timestamp + ".html";

        // k6-Skript aus dem TestType bestimmen (test.js oder messaging-e2e-test.js)
        String k6Script = K6_SCRIPT_DIR + testType.getScript();

        Map<String, String> effectiveEnv = new LinkedHashMap<>();
        effectiveEnv.put("ORCHESTRATOR_URL", orchestratorBaseUrl);
        effectiveEnv.put("STORE_URL", storeBaseUrl);
        effectiveEnv.put("STORE_ID", String.valueOf(storeId));
        effectiveEnv.put("REGISTER_IDS", registerIds);
        effectiveEnv.put("CASHIER_IDS", cashierIds);
        effectiveEnv.putAll(extraEnv);

        // Basis-Kommando zusammenbauen
        // TODO: keycloak
        List<String> command = new ArrayList<>(List.of(
                "docker", "exec",
                "-e", "K6_WEB_DASHBOARD_EXPORT=" + reportFile,
                K6_CONTAINER,
                "k6", "run",
                "-o", PROMETHEUS_RW,
                k6Script,
                "-e", "K6_MASTER_TOKEN=" + UUID.randomUUID(),
                "-e", "SCENARIO=" + testType.getScenarioKey()
        ));

        // Zusätzliche Umgebungsvariablen anhängen (z.B. STORE_ID, ORDER_RATE, …)
        for (Map.Entry<String, String> entry : effectiveEnv.entrySet()) {
            command.add("-e");
            command.add(entry.getKey() + "=" + entry.getValue());
        }

        log.info("");
        log.info(SEP);
        log.info("  VENDIX LASTTEST GESTARTET (k6)");
        log.info("  Szenario  : {}", testType.getDisplayName());
        log.info("  Skript    : {}", k6Script);
        log.info("  Parameter : {}", testType.getParameters());
        log.info("  Extra-Env : {}", effectiveEnv.keySet());
        log.info("  Dauer     : {} Sekunden", testType.getDurationSeconds());
        log.info("  Container : {}", K6_CONTAINER);
        log.info("  Report    : {}", reportFile);
        log.info(SEP);
        log.info("");

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(repositoryRoot.toFile());
        pb.redirectErrorStream(true);

        Process proc;
        try {
            proc = pb.start();
        } catch (IOException e) {
            @Nullable String message = e.getMessage();
            throw new IOException(
                    "docker exec fehlgeschlagen: " + (message == null ? e.getClass().getSimpleName() : message) +
                            "  –  Läuft der k6-Container? (docker ps | grep vendix-k6)", e
            );
        }

        process.set(proc);
        running.set(true);
        activeTest.set(testType);
        startedAt.set(Instant.now().getEpochSecond());

        Thread logThread = new Thread(() -> {
            try (var reader = proc.inputReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("running (")) log.info("[k6] {}", line);
                    logConsumer.accept(line);
                }
            } catch (IOException e) {
                @Nullable String message = e.getMessage();
                String msg = message == null ? "Stream-Fehler" : message;
                logConsumer.accept("[ERROR] Log-Stream unterbrochen: " + msg);
                log.error("[Performance] Log-Stream unterbrochen: {}", msg);
            }

            int exitCode = 0;
            try { exitCode = proc.waitFor(); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            long elapsed = Instant.now().getEpochSecond() - startedAt.get();
            @Nullable TestType finished = activeTest.get();
            String finishedName = finished != null ? finished.getDisplayName() : "Unbekannt";

            log.info("");
            log.info(SEP);
            if (exitCode == 0) {
                log.info("  VENDIX LASTTEST ABGESCHLOSSEN ✓");
            } else if (exitCode == 99) {
                log.warn("  VENDIX LASTTEST: Thresholds nicht erfüllt ⚠");
            } else if (exitCode == 137 || exitCode == 143 || exitCode == 1) {
                log.info("  VENDIX LASTTEST MANUELL GESTOPPT ■");
            } else {
                log.warn("  VENDIX LASTTEST FEHLGESCHLAGEN ✗ (Exit-Code {})", exitCode);
            }
            log.info("  Szenario : {}", finishedName);
            log.info("  Laufzeit : {} min {} sek", elapsed / 60, elapsed % 60);
            log.info("  Report   : .docker/testing/k6/reports/ (auf dem Host)");
            log.info(SEP);
            log.info("");

            logConsumer.accept(String.format(
                    "[FERTIG] %s – Laufzeit: %d min %d sek – Exit-Code: %d",
                    finishedName, elapsed / 60, elapsed % 60, exitCode
            ));
            logConsumer.accept("[REPORT] Gespeichert unter: .docker/testing/k6/reports/");

            running.set(false);
            process.set(null);
            activeTest.set(null);
            startedAt.set(0);
        }, "k6-log-" + testType.name());
        logThread.setDaemon(true);
        logThread.start();
    }

    // ─── Stoppen ──────────────────────────────────────────────────────────────

    public void stopTest() {
        @Nullable Process proc = process.get();
        if (proc != null && proc.isAlive()) {
            proc.destroyForcibly();
            log.info("[Performance] k6-Prozess manuell gestoppt.");
            try {
                new ProcessBuilder("docker", "exec", K6_CONTAINER, "pkill", "-f", "k6 run")
                        .redirectErrorStream(true).start().waitFor();
            } catch (IOException | InterruptedException e) {
                log.debug("pkill fehlgeschlagen (ok): {}", e.getMessage());
            }
        }
        running.set(false);
    }

    // ─── Status ───────────────────────────────────────────────────────────────

    public boolean isRunning() { return running.get(); }

    public Optional<TestType> getActiveTest() { return Optional.ofNullable(activeTest.get()); }

    public long getElapsedSeconds() {
        long start = startedAt.get();
        return start > 0 ? Instant.now().getEpochSecond() - start : 0;
    }

    public double getProgress() {
        @Nullable TestType test = activeTest.get();
        if (test == null || !running.get()) return 0.0;
        return Math.min(1.0, (double) getElapsedSeconds() / test.getDurationSeconds());
    }

    // ─── Hilfsmethoden ────────────────────────────────────────────────────────

    private Path resolveRepositoryRoot() throws IOException {
        Path startPath = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        for (@Nullable Path current = startPath; current != null; current = current.getParent()) {
            if (Files.exists(current.resolve(K6_COMPOSE))) return current;
        }
        throw new IOException(
                "Projektwurzel nicht gefunden. Erwartet: " + K6_COMPOSE + " in einem Elternverzeichnis.");
    }

    private void ensureK6ContainerRunning(Path repositoryRoot, Consumer<String> logConsumer) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "docker", "compose",
                "--env-file", repositoryRoot.resolve(".docker/.env").toString(),
                "-f", repositoryRoot.resolve(K6_COMPOSE).toString(),
                "up", "-d", "k6"
        );
        pb.directory(repositoryRoot.toFile());
        pb.redirectErrorStream(true);

        Process composeProcess = pb.start();
        try (var reader = composeProcess.inputReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                logConsumer.accept("[docker-compose] " + line);
            }
        }
        try {
            int exitCode = composeProcess.waitFor();
            if (exitCode != 0) throw new IOException(
                    "docker compose konnte k6-Container nicht starten (Exit-Code " + exitCode + ").");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Warten auf docker compose unterbrochen.", e);
        }
    }
}
