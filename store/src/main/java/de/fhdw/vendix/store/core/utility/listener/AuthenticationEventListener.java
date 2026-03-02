package de.fhdw.vendix.store.core.utility.listener;

import de.fhdw.vendix.commons.core.persistence.entity.LockTypeEnum;
import de.fhdw.vendix.store.core.domain.lock.Lock;
import de.fhdw.vendix.store.core.domain.lock.LockService;
import de.fhdw.vendix.store.core.utility.StoreClient;
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

    private final LockService lockService;
    private final StoreClient storeClient;

    public AuthenticationEventListener(LockService lockService, StoreClient storeClient) {
        this.lockService = lockService;
        this.storeClient = storeClient;
    }

    @Async
    @EventListener
    public void onAuthenticationSuccess(InteractiveAuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        if (!(authentication.getPrincipal() instanceof AuthContext context)) {
            return;
        }

        Lock lock = new Lock(
                LockTypeEnum.ACCOUNT,
                context.getAccountId(),
                storeClient.getInstanceId(),
                Instant.now().plus(1, ChronoUnit.HOURS)
        );

        try {
            lockService.create(lock);
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
            lockService.deleteByLockTypeAndTargetId(LockTypeEnum.ACCOUNT, context.getAccountId());
        } catch (Exception ex) {
            log.atError().log("Failed to delete distributed lock", ex);
        }
    }
}