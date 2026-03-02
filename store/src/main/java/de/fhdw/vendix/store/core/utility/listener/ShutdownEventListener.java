package de.fhdw.vendix.store.core.utility.listener;

import de.fhdw.vendix.store.core.domain.lock.LockService;
import de.fhdw.vendix.store.core.utility.StoreClient;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ShutdownEventListener {

    private final LockService lockService;
    private final StoreClient storeClient;

    public ShutdownEventListener(LockService lockService, StoreClient storeClient) {
        this.lockService = lockService;
        this.storeClient = storeClient;
    }

    @EventListener
    public void onContextClosed(ContextClosedEvent event) {
        lockService.deleteAllByOwnerInstance(storeClient.getInstanceId());
    }
}