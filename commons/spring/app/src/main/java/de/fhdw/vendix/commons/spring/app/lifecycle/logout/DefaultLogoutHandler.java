package de.fhdw.vendix.commons.spring.app.lifecycle.logout;

import de.fhdw.vendix.commons.api.embeddable.TargetType;
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


    // TODO

    @EventListener
    @Override
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        try {
            log.atInfo().log("Deleting lock for logged out account...");
//            if (!(event.getAuthentication().getPrincipal() instanceof DefaultUser defaultUser)) {
//                throw new IllegalStateException("Authentication principal is not an instance of DefaultUser");
//            }
//            distributedLockProxyService.deleteDistributedLockByTarget(
//                    TargetType.ACCOUNT,
//                    Objects.requireNonNull(defaultUser.authContext().account().id())
//            );
            log.atInfo().log("Successfully deleted lock for logged out account");
        } catch (Exception ex) {
            log.atError().log("Failed to delete distributed lock", ex);
        }
    }
}
