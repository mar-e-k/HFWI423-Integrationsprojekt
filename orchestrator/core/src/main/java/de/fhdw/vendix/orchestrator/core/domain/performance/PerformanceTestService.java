package de.fhdw.vendix.orchestrator.core.domain.performance;

import de.fhdw.vendix.commons.spring.security.jwt.JwtService;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Startet und stoppt k6-Lasttests via Docker.
 *
 * Features:
 *  - k6 läuft im Container vendix-k6
 *  - Metriken werden live nach Prometheus gepusht (experimental-prometheus-rw)
 *  - Am Ende jedes Tests wird automatisch ein HTML-Report gespeichert
 *    (K6_WEB_DASHBOARD_EXPORT) — sichtbar im k6 Live Dashboard und als Datei
 */
@Service
public class PerformanceTestService {

    private static final Logger log       = LoggerFactory.getLogger(PerformanceTestService.class);
    private static final String SEP       = "═".repeat(70);
    private static final String K6_CONTAINER  = "vendix-k6";
    private static final String K6_SCRIPT     = "/etc/k6/scripts/test.js";
    private static final String K6_COMPOSE    = ".docker/testing/docker-compose.yaml";
    private static final String PROMETHEUS_RW = "experimental-prometheus-rw";

    // HTML-Report wird im Container unter diesem Pfad gespeichert.
    // Der Ordner /etc/k6/reports ist im Volume gemountet → nach dem Test
    // findest du die Datei in .docker/testing/k6/reports/ auf dem Host.
    private static final String REPORT_DIR = "/etc/k6/reports";

    private static final DateTimeFormatter REPORT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm").withZone(ZoneId.systemDefault());

    private final JwtService jwtService;
    private final AtomicBoolean                       running    = new AtomicBoolean(false);
    private final AtomicReference<@Nullable Process>     process    = new AtomicReference<>(null);
    private final AtomicReference<@Nullable TestType>    activeTest = new AtomicReference<>(null);
    private final AtomicLong                          startedAt  = new AtomicLong(0);

    public PerformanceTestService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    // ─── Starten ──────────────────────────────────────────────────────────────

    public void startTest(TestType testType, Consumer<String> logConsumer) throws IOException {
        if (running.get()) {
            TestType current = activeTest.get();
            String name = current != null ? current.getDisplayName() : "Unbekannt";
            throw new IllegalStateException("Ein Test läuft bereits: " + name);
        }

        Path repositoryRoot = resolveRepositoryRoot();
        ensureK6ContainerRunning(repositoryRoot, logConsumer);

        // Report-Dateiname: z.B. vendix-lasttest_2026-04-17_14-30.html
        String timestamp  = REPORT_FMT.format(Instant.now());
        String reportFile = REPORT_DIR + "/vendix-" + testType.getScenarioKey() + "_" + timestamp + ".html";

        // docker exec -e K6_WEB_DASHBOARD_EXPORT=<file> vendix-k6 k6 run ...
        // K6_WEB_DASHBOARD_EXPORT: k6 schreibt am Ende automatisch einen vollständigen
        // HTML-Report ins angegebene File — enthält alle Metriken, Charts, Thresholds.
        List<String> command = List.of(
                "docker", "exec",
                "-e", "K6_WEB_DASHBOARD_EXPORT=" + reportFile,
                K6_CONTAINER,
                "k6", "run",
                "-o", PROMETHEUS_RW,
                K6_SCRIPT,
                "-e", "K6_MASTER_TOKEN=" + jwtService.generateToken(),
                "-e", "SCENARIO=" + testType.getScenarioKey()
        );

        log.info("");
        log.info(SEP);
        log.info("  VENDIX LASTTEST GESTARTET (k6)");
        log.info("  Szenario  : {}", testType.getDisplayName());
        log.info("  Parameter : {}", testType.getParameters());
        log.info("  Dauer     : {} Sekunden", testType.getDurationSeconds());
        log.info("  Container : {}", K6_CONTAINER);
        log.info("  Report    : {}", reportFile);
        log.info("  Befehl    : {}", String.join(" ", command));
        log.info(SEP);
        log.info("");

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(repositoryRoot.toFile());
        pb.redirectErrorStream(true);

        Process proc;
        try {
            proc = pb.start();
        } catch (IOException e) {
            throw new IOException(
                    "docker exec fehlgeschlagen: " + e.getMessage() +
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
                String msg = e.getMessage() != null ? e.getMessage() : "Stream-Fehler";
                logConsumer.accept("[ERROR] Log-Stream unterbrochen: " + msg);
                log.error("[Performance] Log-Stream unterbrochen: {}", msg);
            }

            int exitCode = 0;
            try { exitCode = proc.waitFor(); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            long elapsed = Instant.now().getEpochSecond() - startedAt.get();
            TestType finished = activeTest.get();
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
        Process proc = process.get();
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
        TestType test = activeTest.get();
        if (test == null || !running.get()) return 0.0;
        return Math.min(1.0, (double) getElapsedSeconds() / test.getDurationSeconds());
    }

    // ─── Hilfsmethoden ────────────────────────────────────────────────────────

    private Path resolveRepositoryRoot() throws IOException {
        Path startPath = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        for (Path current = startPath; current != null; current = current.getParent()) {
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