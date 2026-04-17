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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Startet und stoppt k6-Lasttests plattformunabhängig (macOS, Linux, Windows).
 *
 * Arbeitet mit Docker Compose: der k6-Container muss laufen (siehe .docker/testing).
 * Der Service ruft "docker exec vendix-k6 k6 run ..." auf — keine lokale k6-Installation nötig.
 *
 * Vorteile gegenüber JMeter:
 *  - keine Java-Versionsprobleme (k6 ist in Go geschrieben)
 *  - keine .bat-Wrapper, keine Pfad-Separator-Probleme
 *  - k6 pusht Metriken direkt an Prometheus (experimental-prometheus-rw)
 */
@Service
public class PerformanceTestService {

    private static final Logger log = LoggerFactory.getLogger(PerformanceTestService.class);
    private static final String SEP = "═".repeat(70);

    /** Name des k6-Containers (muss in .docker/testing/docker-compose.yaml gesetzt sein) */
    private static final String K6_CONTAINER  = "vendix-k6";
    private static final String K6_SCRIPT     = "/etc/k6/scripts/test.js";
    private static final String K6_COMPOSE    = ".docker/testing/docker-compose.yaml";
    private static final String PROMETHEUS_RW = "experimental-prometheus-rw";

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

        // docker exec vendix-k6 k6 run -o experimental-prometheus-rw \
        //   /etc/k6/scripts/test.js -e SCENARIO=lasttest
        List<String> command = List.of(
                "docker", "exec", K6_CONTAINER,
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
                    // k6 Progress- und Summary-Zeilen hervorheben
                    if (line.contains("running (")) {
                        log.info("[k6] {}", line);
                    }
                    logConsumer.accept(line);
                }
            } catch (IOException e) {
                String msg = e.getMessage() != null ? e.getMessage() : "Stream-Fehler";
                logConsumer.accept("[ERROR] Log-Stream unterbrochen: " + msg);
                log.error("[Performance] Log-Stream unterbrochen: {}", msg);
            }

            int exitCode = 0;
            try {
                exitCode = proc.waitFor();
            } catch (InterruptedException e) {
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
                // k6 Exit-Code 99 = Thresholds fehlgeschlagen (Test lief, aber KPI verfehlt)
                log.warn("  VENDIX LASTTEST: Thresholds nicht erfüllt ⚠");
            } else if (exitCode == 137 || exitCode == 143 || exitCode == 1) {
                log.info("  VENDIX LASTTEST MANUELL GESTOPPT ■");
            } else {
                log.warn("  VENDIX LASTTEST FEHLGESCHLAGEN ✗ (Exit-Code {})", exitCode);
            }
            log.info("  Szenario : {}", finishedName);
            log.info("  Laufzeit : {} min {} sek", elapsed / 60, elapsed % 60);
            log.info(SEP);
            log.info("");

            logConsumer.accept(String.format(
                    "[FERTIG] %s – Laufzeit: %d min %d sek – Exit-Code: %d",
                    finishedName, elapsed / 60, elapsed % 60, exitCode
            ));

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

            // Zusätzlich k6 im Container killen falls docker exec hängt
            try {
                new ProcessBuilder("docker", "exec", K6_CONTAINER, "pkill", "-f", "k6 run")
                        .redirectErrorStream(true)
                        .start()
                        .waitFor();
            } catch (IOException | InterruptedException e) {
                log.debug("pkill im Container fehlgeschlagen (ok wenn kein k6 lief): {}", e.getMessage());
            }
        }
        running.set(false);
    }

    // ─── Status ───────────────────────────────────────────────────────────────

    public boolean isRunning() {
        return running.get();
    }

    public Optional<TestType> getActiveTest() {
        return Optional.ofNullable(activeTest.get());
    }

    public long getElapsedSeconds() {
        long start = startedAt.get();
        return start > 0 ? Instant.now().getEpochSecond() - start : 0;
    }

    public double getProgress() {
        TestType test = activeTest.get();
        if (test == null || !running.get()) return 0.0;
        return Math.min(1.0, (double) getElapsedSeconds() / test.getDurationSeconds());
    }

    private Path resolveRepositoryRoot() throws IOException {
        Path startPath = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();

        for (Path current = startPath; current != null; current = current.getParent()) {
            if (Files.exists(current.resolve(K6_COMPOSE))) {
                return current;
            }
        }

        throw new IOException(
                "Projektwurzel nicht gefunden. Erwartet wurde " + K6_COMPOSE + " in einem Elternverzeichnis."
        );
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
            if (exitCode != 0) {
                throw new IOException(
                        "docker compose konnte den k6-Container nicht starten (Exit-Code " + exitCode + ")."
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Warten auf docker compose wurde unterbrochen.", e);
        }
    }
}
