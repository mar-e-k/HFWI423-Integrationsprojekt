package de.fhdw.vendix.commons.spring.app.lifecycle.shutdown;

import de.fhdw.vendix.commons.spring.security.context.app.AppContext;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.ConnectionProxyService;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.DistributedLockProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

public final class DefaultShutdownHandler implements ShutdownHandler{

    private static final Logger log = LoggerFactory.getLogger(DefaultShutdownHandler.class);

    private final AppContext appContext;
    private final DistributedLockProxyService distributedLockProxyService;
    private final ConnectionProxyService  connectionProxyService;

    public DefaultShutdownHandler(AppContext appContext, DistributedLockProxyService distributedLockProxyService, ConnectionProxyService connectionProxyService) {
        this.appContext = appContext;
        this.distributedLockProxyService = distributedLockProxyService;
        this.connectionProxyService = connectionProxyService;
    }


    @EventListener
    @Order(0)
    @Override
    public void deleteLocksOnShutdown(ContextClosedEvent event) {
        try {
            log.atInfo().log("Deleting application instance locks...");
            distributedLockProxyService.deleteAllDistributedLocksByInstanceUUID(appContext.getInstanceUUID());
            log.atInfo().log("Successfully deleted application instance locks");
        } catch (Exception e) {
            log.atError().log("Failed to delete application instance locks", e);
        }
    }

    @EventListener
    @Order(1)
    @Override
    public void deleteConnectionOnShutdown(ContextClosedEvent event) {
        try {
            log.atInfo().log("Deleting application instance connection...");
            connectionProxyService.deleteConnectionByInstanceUuid(appContext.getInstanceUUID());
            log.atInfo().log("Successfully deleted application instance connection");
        } catch (Exception e) {
            log.atError().log("Failed to delete application instance connection", e);
        }
    }
}