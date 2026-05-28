package de.fhdw.vendix.orchestrator.ui.performance;

import de.fhdw.vendix.commons.spring.vaadin.utility.DateTimeFormat;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PerformanceTestLauncher {

    private static final Logger LOG = LoggerFactory.getLogger(PerformanceTestLauncher.class);

    private final String configuredWorkingDirectory;
    private final String composeFile;
    private final String orchestratorBaseUrl;
    private final String storeBaseUrl;
    private final long storeId;
    private final String registerIds;
    private final String cashierIds;

    private final Map<PerformanceTestType, ProcessHandle> runningProcesses = new ConcurrentHashMap<>();
    private final Map<PerformanceTestType, PerformanceTestStatus> statuses = new ConcurrentHashMap<>();

    public PerformanceTestLauncher(
            @Value("${vendix.loadtests.working-directory:}") String configuredWorkingDirectory,
            @Value("${vendix.loadtests.compose-file:.docker/testing/docker-compose.yaml}") String composeFile,
            @Value("${vendix.loadtests.orchestrator-base-url:http://host.docker.internal:8080}") String orchestratorBaseUrl,
            @Value("${vendix.loadtests.store-base-url:http://host.docker.internal:8081}") String storeBaseUrl,
            @Value("${vendix.loadtests.store-id:1}") long storeId,
            @Value("${vendix.loadtests.register-ids:1,2,3}") String registerIds,
            @Value("${vendix.loadtests.cashier-ids:1,2,3}") String cashierIds
    ) {
        this.configuredWorkingDirectory = configuredWorkingDirectory;
        this.composeFile = composeFile;
        this.orchestratorBaseUrl = orchestratorBaseUrl;
        this.storeBaseUrl = storeBaseUrl;
        this.storeId = storeId;
        this.registerIds = registerIds;
        this.cashierIds = cashierIds;

        for (PerformanceTestType type : PerformanceTestType.values()) {
            statuses.put(type, new PerformanceTestStatus(type, false, "Bereit", null, null, null));
        }
    }

    public synchronized PerformanceTestLaunchResult launch(PerformanceTestType type) {
        ProcessHandle runningProcess = runningProcesses.get(type);
        if (runningProcess != null && runningProcess.isAlive()) {
            PerformanceTestStatus status = statuses.computeIfAbsent(
                    type,
                    missingType -> new PerformanceTestStatus(missingType, false, "Bereit", null, null, null)
            );
            return new PerformanceTestLaunchResult(
                    false,
                    type.label() + " laeuft bereits. Log: " + formatPath(status.logFile()),
                    status.logFile()
            );
        }

        try {
            Path repositoryRoot = resolveRepositoryRoot();
            ensureK6ContainerRunning(repositoryRoot);
            Path logFile = prepareLogFile(repositoryRoot, type);
            List<String> command = buildCommand(repositoryRoot, type);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(repositoryRoot.toFile());
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(Redirect.appendTo(logFile.toFile()));

            Process process = processBuilder.start();
            Instant startedAt = Instant.now();

            runningProcesses.put(type, process.toHandle());
            statuses.put(type, new PerformanceTestStatus(type, true, "Laeuft", startedAt, null, logFile));

            CompletableFuture<Void> unused =
                    process.onExit().thenAccept(exitedProcess -> handleExit(type, exitedProcess.exitValue(), startedAt, logFile));

            LOG.info("Started {} with command {}", type.label(), command);
            return new PerformanceTestLaunchResult(
                    true,
                    type.label() + " wurde gestartet. Log: " + logFile,
                    logFile
            );
        } catch (Exception exception) {
            LOG.error("Failed to start {}", type.label(), exception);
            statuses.put(type, new PerformanceTestStatus(type, false, "Start fehlgeschlagen", Instant.now(), -1, null));

            return new PerformanceTestLaunchResult(
                    false,
                    type.label() + " konnte nicht gestartet werden: " + exceptionMessage(exception),
                    null
            );
        }
    }

    public List<PerformanceTestStatus> statuses() {
        return statuses.values().stream()
                .sorted(Comparator.comparing(PerformanceTestStatus::type))
                .toList();
    }

    private void handleExit(PerformanceTestType type, int exitCode, Instant startedAt, Path logFile) {
        runningProcesses.remove(type);
        statuses.put(
                type,
                new PerformanceTestStatus(
                        type,
                        false,
                        exitCode == 0 ? "Beendet" : "Fehlgeschlagen",
                        startedAt,
                        exitCode,
                        logFile
                )
        );
        LOG.info("{} finished with exit code {}", type.label(), exitCode);
    }

    private Path resolveRepositoryRoot() {
        Path startPath = configuredWorkingDirectory.isBlank()
                ? Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize()
                : Path.of(configuredWorkingDirectory).toAbsolutePath().normalize();

        for (@Nullable Path current = startPath; current != null; current = current.getParent()) {
            if (Files.exists(current.resolve(".docker/testing/docker-compose.yaml"))) {
                return current;
            }
        }

        throw new IllegalStateException(
                "Projektwurzel nicht gefunden. Setze vendix.loadtests.working-directory auf das Repository."
        );
    }

    private Path prepareLogFile(Path repositoryRoot, PerformanceTestType type) throws IOException {
        Path logDirectory = repositoryRoot.resolve("orchestrator/app/target/k6-logs");
        Files.createDirectories(logDirectory);

        String timestamp = DateTimeFormat.UI_TIME.format(Instant.now());
        return logDirectory.resolve(type.name().toLowerCase(Locale.ROOT) + "-" + timestamp + ".log");
    }

    private List<String> buildCommand(Path repositoryRoot, PerformanceTestType type) {
        Path composeFilePath = resolveComposeFile(repositoryRoot);
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("compose");
        command.add("-f");
        command.add(composeFilePath.toString());
        command.add("exec");
        command.add("-T");
        command.add("-e");
        command.add("SCENARIO=" + type.scenarioKey());
        command.add("-e");
        // todo: keycloak
        command.add("K6_MASTER_TOKEN=" + UUID.randomUUID().toString());
        command.add("-e");
        command.add("ORCHESTRATOR_URL=" + orchestratorBaseUrl);
        command.add("-e");
        command.add("STORE_URL=" + storeBaseUrl);
        command.add("-e");
        command.add("STORE_ID=" + storeId);
        command.add("-e");
        command.add("REGISTER_IDS=" + registerIds);
        command.add("-e");
        command.add("CASHIER_IDS=" + cashierIds);
        command.add("k6");
        command.add("k6");
        command.add("run");
        command.add("-o");
        command.add("experimental-prometheus-rw");
        command.add("/etc/k6/scripts/test.js");
        return command;
    }

    private void ensureK6ContainerRunning(Path repositoryRoot) throws IOException, InterruptedException {
        Path composeFilePath = resolveComposeFile(repositoryRoot);
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "compose",
                "-f", composeFilePath.toString(),
                "up", "-d", "k6"
        );
        processBuilder.directory(repositoryRoot.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("k6-Container konnte nicht gestartet werden (Exit-Code " + exitCode + ").");
        }
    }

    private Path resolveComposeFile(Path repositoryRoot) {
        Path configuredComposeFile = Path.of(composeFile);
        if (configuredComposeFile.isAbsolute()) {
            return configuredComposeFile;
        }
        return repositoryRoot.resolve(configuredComposeFile).normalize();
    }

    private String formatPath(@Nullable Path path) {
        return path == null ? "-" : path.toString();
    }

    private static String exceptionMessage(Exception exception) {
        @Nullable String message = exception.getMessage();
        return message == null ? exception.getClass().getSimpleName() : message;
    }
}
