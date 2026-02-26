package de.fhdw.vendix.store.utility.listener;

import de.fhdw.vendix.commons.core.persistence.entity.LockTypeEnum;
import de.fhdw.vendix.store.persistence.entity.DistributedLock;
import de.fhdw.vendix.store.persistence.service.DistributedLockService;
import de.fhdw.vendix.store.utility.StoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class AuthenticationEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationEventListener.class);

    private final DistributedLockService distributedLockService;
    private final StoreClient storeClient;

    public AuthenticationEventListener(DistributedLockService distributedLockService, StoreClient storeClient) {
        this.distributedLockService = distributedLockService;
        this.storeClient = storeClient;
    }

    @Async
    @EventListener
    public void onAuthenticationSuccess(InteractiveAuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        if (!(authentication.getPrincipal() instanceof AuthContext context)) {
            return;
        }

        DistributedLock lock = new DistributedLock(
                LockTypeEnum.ACCOUNT,
                context.getAccountId(),
                storeClient.getInstanceId(),
                Instant.now().plus(1, ChronoUnit.HOURS)
        );

        try {
            distributedLockService.create(lock);
        } catch (Exception ex) {
            log.atError().log("Failed to create distributed lock", ex);
        }
    }

    @Async
    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthContext context)) {
            return;
        }

        try {
            distributedLockService.deleteByLockTypeAndTargetId(LockTypeEnum.ACCOUNT, context.getAccountId());
        } catch (Exception ex) {
            log.atError().log("Failed to delete distributed lock", ex);
        }
    }
}