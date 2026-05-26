package de.fhdw.vendix.commons.spring.app.lifecycle.login;

import de.fhdw.vendix.commons.api.domain.distributed_lock.DistributedLockDTO;
import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.app.lifecycle.app.AppContext;
import de.fhdw.vendix.commons.spring.security.context.auth.DefaultUser;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.DistributedLockProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class DefaultLoginHandler implements LoginHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultLoginHandler.class);
    private static final Duration ACCOUNT_LOCK_TTL = Duration.ofHours(1);

    private final AppContext appContext;
    private final DistributedLockProxyService distributedLockProxyService;

    public DefaultLoginHandler(AppContext appContext, DistributedLockProxyService distributedLockProxyService) {
        this.appContext = appContext;
        this.distributedLockProxyService = distributedLockProxyService;
    }


    @EventListener
    @Override
    public void onLoginSuccess(InteractiveAuthenticationSuccessEvent event) {
        try {
            log.atInfo().log("Creating lock for logged in account...");
            if (!(event.getAuthentication().getPrincipal() instanceof DefaultUser defaultUser)) {
                throw new IllegalStateException("Authentication principal is not an instance of DefaultUser");
            }
            Long accountId = Objects.requireNonNull(defaultUser.authContext().account().id());

            EntityTargetDTO entityTargetDTO = new EntityTargetDTO(
                    accountId,
                    TargetType.ACCOUNT
            );

            Instant acquiredAt = Instant.now();
            DistributedLockDTO lock = new DistributedLockDTO(
                    null,
                    entityTargetDTO,
                    appContext.getInstanceUUID(),
                    acquiredAt,
                    acquiredAt.plus(ACCOUNT_LOCK_TTL)
            );

            distributedLockProxyService.postDistributedLock(lock);
            log.atInfo().log("Successfully created lock for logged in account");
        } catch (Exception ex) {
            log.atError().log("Failed to create lock for logged in account", ex);
        }
    }
}
