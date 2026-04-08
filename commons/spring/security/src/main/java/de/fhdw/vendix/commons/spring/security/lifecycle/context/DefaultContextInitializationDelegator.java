package de.fhdw.vendix.commons.spring.security.lifecycle.context;

import de.fhdw.vendix.commons.api.domain.connection.ConnectionDTO;
import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.api.embeddable.InstanceDetailsDTO;
import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.security.context.app.AppContext;
import de.fhdw.vendix.commons.spring.security.context.register.RegisterContextInitializedEvent;
import de.fhdw.vendix.commons.spring.security.context.store.StoreContextInitializedEvent;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.ConnectionProxyService;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.LockProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class DefaultContextInitializationDelegator implements ContextInitializationDelegator {

    private static final Logger log = LoggerFactory.getLogger(DefaultContextInitializationDelegator.class);

    private final AppContext appContext;
    private final ConnectionProxyService connectionProxyService;
    private final LockProxyService lockProxyService;

    public DefaultContextInitializationDelegator(
            AppContext appContext,
            ConnectionProxyService connectionProxyService,
            LockProxyService lockProxyService
    ) {
        this.appContext = appContext;
        this.connectionProxyService = connectionProxyService;
        this.lockProxyService = lockProxyService;
    }

    @EventListener
    @Override
    public void onRegisterContextInitializedEvent(RegisterContextInitializedEvent event) {
        EntityTargetDTO entityTarget = new EntityTargetDTO(
                Objects.requireNonNull(event.getRegister().id()),
                TargetType.REGISTER
        );
        registerConnectionAndLock(entityTarget);
    }

    @EventListener
    @Override
    public void onStoreContextInitializedEvent(StoreContextInitializedEvent event) {
        EntityTargetDTO entityTarget = new EntityTargetDTO(
                Objects.requireNonNull(event.getStore().id()),
                TargetType.STORE
        );
        registerConnectionAndLock(entityTarget);
    }

    private void registerConnectionAndLock(EntityTargetDTO entityTarget) {
        try {
            MDC.put("__target_type__", entityTarget.type().name());
            MDC.put("__target_id__", entityTarget.id().toString());

            log.atInfo().log("Starting registration of connection and lock...");
            Instant now = Instant.now();

            ConnectionDTO connection = new ConnectionDTO(
                    null,
                    entityTarget,
                    new InstanceDetailsDTO(
                            appContext.getInstanceUUID(),
                            appContext.getHostname(),
                            appContext.getServerName(),
                            appContext.getServerPort()
                    ),
                    now,
                    now.plus(1, ChronoUnit.SECONDS)
            );
            log.atDebug().log("Registering Application-Connection...");
            connectionProxyService.postConnection(connection);
            log.atDebug().log("Successfully registered Application-Connection");

            LockDTO lock = new LockDTO(
                    null,
                    entityTarget,
                    appContext.getInstanceUUID(),
                    now,
                    now.plus(1, ChronoUnit.HOURS)
            );
            log.atDebug().log("Registering Application-Lock...");
            lockProxyService.postLock(lock);
            log.atDebug().log("Successfully registered Application-Lock");

            log.atInfo().log("Successfully registered connection and lock");
        } catch (Exception e) {
            log.atError().log("Failed to register connection and lock", e);
        } finally {
            MDC.remove("__target_type__");
            MDC.remove("__target_id__");
        }
    }
}