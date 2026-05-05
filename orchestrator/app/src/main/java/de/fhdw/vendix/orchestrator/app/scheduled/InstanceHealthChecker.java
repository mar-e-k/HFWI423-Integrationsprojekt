package de.fhdw.vendix.orchestrator.app.scheduled;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
class InstanceHealthChecker {

//    private static final Logger log = LoggerFactory.getLogger(InstanceHealthChecker.class);
//
//    private final ConnectionService connectionService;
//    private final RestTemplate restTemplate;
//    private final ExecutorService executorService;
//
//    private static final int FAILURE_THRESHOLD = 3;
//    private static final int TIMEOUT_MS = 3000;
//
//    public InstanceHealthChecker(ConnectionService connectionService) {
//        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
//        factory.setConnectTimeout(TIMEOUT_MS);
//        factory.setReadTimeout(TIMEOUT_MS);
//        this.restTemplate = new RestTemplate(factory);
//        this.executorService = Executors.newFixedThreadPool(10);
//        this.connectionService = connectionService;
//    }
//
//    @Scheduled(fixedDelay = 60_000)
//    public void check() {
//        log.debug("Starting health check cycle...");
//
//        Set<Connection> successfulPings = ConcurrentHashMap.newKeySet();
//        Set<Connection> failedPings = ConcurrentHashMap.newKeySet();
//
//        List<Connection> connections = connectionService.findAll();
//        Set<CompletableFuture<Void>> futures = connections.stream()
//                .map(connection -> CompletableFuture.runAsync(
//                        () -> pingSingleApplicationInstance(connection, successfulPings, failedPings),
//                        executorService)
//                )
//                .collect(Collectors.toUnmodifiableSet());
//
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//
//        connectionService.updateAll(successfulPings);
//        connectionService.deleteAll(failedPings); // TODO: We might want to mark them instead of just deleting them
//
//        log.debug("Completed health check cycle");
//    }
//
//    private void pingSingleApplicationInstance(Connection connection, Set<Connection> success, Set<Connection> failure) {
//        String url = connection.getInstance().generateInstanceURL() + "actuator/health";
//
//        for (int attempt = 1; attempt <= FAILURE_THRESHOLD; attempt++) {
//            try {
//                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
//
//                if (response.getStatusCode().is2xxSuccessful()
//                        && response.getBody() != null
//                        && "UP".equalsIgnoreCase(String.valueOf(response.getBody().get("status")))) {
//
//                    handleSuccess(connection, success);
//                    return;
//                } else {
//                    throw new IllegalStateException("Health endpoint not UP");
//                }
//            } catch (Exception e) {
//                if (attempt == FAILURE_THRESHOLD) {
//                    handleFailure(connection, failure);
//                    return;
//                } else {
//                    log.atWarn().log("Cannot reach instance {}. Attempt {} of {} exhausted", connection, attempt, FAILURE_THRESHOLD);
//                }
//                try {
//                    Thread.sleep(1000 * attempt);
//                } catch (InterruptedException interruptedException) {
//                    Thread.currentThread().interrupt();
//                    return;
//                }
//            }
//        }
//    }
//
//    private void handleSuccess(Connection connection, Set<Connection> success) {
//        log.atDebug().log("Successfully reached Instance {}", connection);
//        success.add(connection.withHeartbeatAt(Instant.now()));
//    }
//
//    private void handleFailure(Connection connection, Set<Connection> failure) {
//        log.atWarn().log("Failed to reach Instance {}", connection);
//        failure.add(connection);
//    }
//
//    @PreDestroy
//    public void shutdown() {
//        executorService.shutdown();
//    }
}