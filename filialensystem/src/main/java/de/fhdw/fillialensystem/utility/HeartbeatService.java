package de.fhdw.fillialensystem.utility;

import de.fhdw.fillialensystem.persistence.entity.StoreWatcher;
import de.fhdw.fillialensystem.persistence.service.StoreWatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class HeartbeatService {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatService.class);

    private final StoreWatcherService storeWatcherService;
    private final InstanceProvider instanceProvider;
    private final ShutdownManager shutdownManager;

    public HeartbeatService(StoreWatcherService storeWatcherService, InstanceProvider instanceProvider, ShutdownManager shutdownManager) {
        this.storeWatcherService = storeWatcherService;
        this.instanceProvider = instanceProvider;
        this.shutdownManager = shutdownManager;
    }

    @Scheduled(fixedRateString = "${app.heartbeat.rate:10000}", initialDelayString = "${app.heartbeat.initialDelay:5000}")
    public void sendHeartbeat() {
        if (shutdownManager.isShutdownInitiated()) {
            log.info("Shutdown in progress. Skipping heartbeat.");
            return;
        }
        try {
            String instanceId = instanceProvider.getInstanceId();
            Optional<StoreWatcher> watcherOpt = storeWatcherService.findByInstanceId(instanceId);
            watcherOpt.ifPresent(watcher -> {
                watcher.setLastHeartbeatTimestamp(Instant.now());
                storeWatcherService.save(watcher);
                log.debug("Heartbeat sent for instance {}.", instanceId);
            });
        } catch (Exception ex) {
            log.warn("Failed to send heartbeat: {}", ex.getMessage());
        }
    }
}
