package de.fhdw.vendix.pos.utility.listener;

import de.fhdw.vendix.pos.persistance.service.proxy.DistributedLockProxyService;
import de.fhdw.vendix.pos.utility.RegisterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ShutdownEventListener {

    private static final Logger log = LoggerFactory.getLogger(ShutdownEventListener.class);

    private final DistributedLockProxyService distributedLockProxyService;
    private final RegisterClient registerClient;

    public ShutdownEventListener(DistributedLockProxyService distributedLockProxyService, RegisterClient registerClient) {
        this.distributedLockProxyService = distributedLockProxyService;
        this.registerClient = registerClient;
    }

    @Async
    @EventListener
    public void onContextClosed(ContextClosedEvent event) {
        distributedLockProxyService
                .deleteAllByOwnerInstance(registerClient.getInstanceId())
                .doOnError(ex -> log.atError().log("Failed to delete distributed lock by owner instance", ex))
                .subscribe();
    }
}