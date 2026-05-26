package de.fhdw.vendix.commons.spring.app.lifecycle.logout;

import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.security.context.auth.DefaultUser;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.DistributedLockProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.LogoutSuccessEvent;

import java.util.Objects;

public final class DefaultLogoutHandler implements LogoutHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultLogoutHandler.class);

    private final DistributedLockProxyService distributedLockProxyService;

    public DefaultLogoutHandler(DistributedLockProxyService distributedLockProxyService) {
        this.distributedLockProxyService = distributedLockProxyService;
    }


    @EventListener
    @Override
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        try {
            log.atInfo().log("Deleting lock for logged out account...");
            if (!(event.getAuthentication().getPrincipal() instanceof DefaultUser defaultUser)) {
                throw new IllegalStateException("Authentication principal is not an instance of DefaultUser");
            }
            Long accountId = Objects.requireNonNull(defaultUser.authContext().account().id());
            distributedLockProxyService.deleteDistributedLockByTarget(
                    TargetType.ACCOUNT,
                    accountId
            );
            log.atInfo().log("Successfully deleted lock for logged out account");
        } catch (Exception ex) {
            log.atError().log("Failed to delete distributed lock", ex);
        }
    }
}
