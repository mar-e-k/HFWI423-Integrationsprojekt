package de.fhdw.fillialensystem.utility;

import de.fhdw.fillialensystem.persistence.service.StoreLockService;
import de.fhdw.fillialensystem.persistence.service.StoreWatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class ShutdownListener implements ApplicationListener<ContextClosedEvent> {

    private static final Logger log = LoggerFactory.getLogger(ShutdownListener.class);

    private final StoreLockService storeLockService;
    private final StoreWatcherService storeWatcherService;
    private final InstanceProvider instanceProvider;
    private final ShutdownManager shutdownManager;

    public ShutdownListener(StoreLockService storeLockService, StoreWatcherService storeWatcherService, InstanceProvider instanceProvider, ShutdownManager shutdownManager) {
        this.storeLockService = storeLockService;
        this.storeWatcherService = storeWatcherService;
        this.instanceProvider = instanceProvider;
        this.shutdownManager = shutdownManager;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        // Signal that shutdown is starting
        shutdownManager.initiateShutdown();

        String instanceId = instanceProvider.getInstanceId();
        log.info("Application is shutting down. Releasing lock and removing watcher for instance {}.", instanceId);
        try {
            // Find the lock held by this instance and release it.
            storeLockService.findAll().stream()
                    .filter(lock -> lock.getLockedByInstanceId().equals(instanceId))
                    .findFirst()
                    .ifPresent(lock -> {
                        log.info("Releasing lock for store {}.", lock.getStoreId());
                        storeLockService.deleteByStoreId(lock.getStoreId());
                    });

            // Remove this instance from the watcher list.
            storeWatcherService.deleteByInstanceId(instanceId);
            log.info("Watcher for instance {} removed.", instanceId);
        } catch (Exception e) {
            log.error("Error during shutdown cleanup.", e);
        }
    }
}
