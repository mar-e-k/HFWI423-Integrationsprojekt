package de.fhdw.fillialensystem.utility.shutdown;

import de.fhdw.fillialensystem.persistence.service.StoreLinkHostService;
import de.fhdw.fillialensystem.persistence.service.StoreLinkLockService;
import de.fhdw.fillialensystem.utility.StoreClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class StoreCleanupShutdown implements ApplicationListener<ContextClosedEvent> {

    private static final Logger log = LoggerFactory.getLogger(StoreCleanupShutdown.class);

    private final StoreClient storeClient;
    private final boolean cleanupLocksHostsOnShutdown;

    private final StoreLinkLockService storeLinkLockService;
    private final StoreLinkHostService storeLinkHostService;

    public StoreCleanupShutdown(@Value("${spring.filialensystem.shutdown.cleanup-locks-hosts-on-shutdown:true}") boolean cleanupLocksHostsOnShutdown ,
                                StoreClient storeClient,
                                StoreLinkLockService storeLinkLockService,
                                StoreLinkHostService storeLinkHostService) {
        this.cleanupLocksHostsOnShutdown = cleanupLocksHostsOnShutdown;
        this.storeClient = storeClient;
        this.storeLinkLockService = storeLinkLockService;
        this.storeLinkHostService = storeLinkHostService;
    }

    @Override
    public void onApplicationEvent(@NotNull ContextClosedEvent event) {
        log.atInfo().log("spring.filialensystem.startup.cleanup-locks-hosts-on-shutdown is: {}", cleanupLocksHostsOnShutdown);

        if (!cleanupLocksHostsOnShutdown) {
            return;
        }

        if (storeClient.getStore() == null) {
            log.atInfo().log("Store not found in context. Cleanup not needed");
            return;
        }

        try {
            log.atInfo().log("Cleaning up store links");
            storeLinkLockService.deleteByStore(storeClient.getStore());
            log.atInfo().log("Cleaned up store links");
            log.atInfo().log("Cleaning up store hosts");
            storeLinkHostService.deleteByStore(storeClient.getStore());
            log.atInfo().log("Cleaned up store hosts");
        } catch (Exception e) {
            log.atError().log("Error during shutdown cleanup.", e);
        }

        log.atInfo().log("Successfully released locks and removed ping service for context store");
    }
}