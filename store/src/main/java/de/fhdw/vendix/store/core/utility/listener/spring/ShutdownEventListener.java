package de.fhdw.vendix.store.core.utility.listener.spring;

import de.fhdw.vendix.security.api.auth.AppContext;
import de.fhdw.vendix.store.core.domain.lock.LockService;
import de.fhdw.vendix.store.core.utility.StoreContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ShutdownEventListener {

    private final LockService lockService;
    private final AppContext appContext;

    public ShutdownEventListener(LockService lockService, AppContext appContext) {
        this.lockService = lockService;
        this.appContext = appContext;
    }

    @EventListener
    public void onContextClosed(ContextClosedEvent event) {
        lockService.deleteAllByInstanceUUID(appContext.getInstanceUUID());
    }
}