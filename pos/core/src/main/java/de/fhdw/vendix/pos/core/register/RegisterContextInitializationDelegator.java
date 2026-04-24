package de.fhdw.vendix.pos.core.register;

import de.fhdw.vendix.commons.api.domain.connection.ConnectionDTO;
import de.fhdw.vendix.commons.api.domain.connection.ConnectionState;
import de.fhdw.vendix.commons.api.domain.distributed_lock.DistributedLockDTO;
import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.api.embeddable.InstanceDetailsDTO;
import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.app.context.app.AppContext;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.ConnectionProxyService;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.DistributedLockProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Component
public class RegisterContextInitializationDelegator {

    private static final Logger log = LoggerFactory.getLogger(RegisterContextInitializationDelegator.class);

    private final AppContext appContext;
    private final ConnectionProxyService connectionProxyService;
    private final DistributedLockProxyService distributedLockProxyService;

    public RegisterContextInitializationDelegator(
            AppContext appContext,
            ConnectionProxyService connectionProxyService,
            DistributedLockProxyService distributedLockProxyService
    ) {
        this.appContext = appContext;
        this.connectionProxyService = connectionProxyService;
        this.distributedLockProxyService = distributedLockProxyService;
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void onRegisterContextInitializedEvent(RegisterContextInitializedEvent event) {
        EntityTargetDTO entityTarget = new EntityTargetDTO(
                Objects.requireNonNull(event.getRegister().id()),
                TargetType.REGISTER
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
                    now.plus(1, ChronoUnit.SECONDS),
                    0L,
                    ConnectionState.UP
            );
            log.atDebug().log("Registering Application-Connection...");
            ResponseEntity<ConnectionDTO> createdConnection = connectionProxyService.postConnection(connection);
            if (!createdConnection.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("Cannot register POS Connection: " + createdConnection.getStatusCode());
            }
            log.atDebug().log("Successfully registered Application-Connection");

            DistributedLockDTO lock = new DistributedLockDTO(
                    null,
                    entityTarget,
                    appContext.getInstanceUUID(),
                    now,
                    now.plus(1, ChronoUnit.HOURS)
            );
            log.atDebug().log("Registering Application-Lock...");
            ResponseEntity<DistributedLockDTO> createdLock = distributedLockProxyService.postDistributedLock(lock);
            if (!createdLock.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("Cannot register POS Connection: " + createdConnection.getStatusCode());
            }
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