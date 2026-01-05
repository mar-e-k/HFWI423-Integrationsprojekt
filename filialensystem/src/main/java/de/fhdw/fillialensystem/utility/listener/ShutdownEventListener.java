package de.fhdw.fillialensystem.utility.listener;

import de.fhdw.fillialensystem.persistence.service.DistributedLockService;
import de.fhdw.fillialensystem.utility.StoreClient;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ShutdownEventListener {

    private final DistributedLockService distributedLockService;
    private final StoreClient storeClient;

    public ShutdownEventListener(DistributedLockService distributedLockService, StoreClient storeClient) {
        this.distributedLockService = distributedLockService;
        this.storeClient = storeClient;
    }

    @EventListener
    public void onContextClosed(ContextClosedEvent event) {
        distributedLockService.deleteAllByOwnerInstance(storeClient.getInstanceId());
    }
}