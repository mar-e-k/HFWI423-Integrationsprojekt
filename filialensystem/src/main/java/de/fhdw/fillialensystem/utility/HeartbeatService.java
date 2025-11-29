package de.fhdw.fillialensystem.utility;

import de.fhdw.fillialensystem.persistence.entity.StoreWatcher;
import de.fhdw.fillialensystem.persistence.service.StoreWatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@EnableScheduling
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

    @Scheduled(fixedRate = 10000) // every 10 seconds
    public void sendHeartbeat() {
        if (shutdownManager.isShutdownInitiated()) {
            log.info("Shutdown in progress. Skipping heartbeat.");
            return;
        }

        String instanceId = instanceProvider.getInstanceId();
        Optional<StoreWatcher> watcherOpt = storeWatcherService.findByInstanceId(instanceId);
        if (watcherOpt.isPresent()) {
            StoreWatcher watcher = watcherOpt.get();
            watcher.setLastHeartbeatTimestamp(Instant.now());
            storeWatcherService.save(watcher);
            log.debug("Heartbeat sent for instance {}.", instanceId);
        } else {
            log.warn("Cannot send heartbeat. Watcher for instance {} not found.", instanceId);
        }
    }
}
