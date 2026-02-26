package de.fhdw.vendix.pos.utility.listener;

import de.fhdw.vendix.commons.core.api.dto.DistributedLockDTO;
import de.fhdw.vendix.commons.core.persistence.entity.LockTypeEnum;
import de.fhdw.vendix.pos.persistance.service.proxy.DistributedLockProxyService;
import de.fhdw.vendix.pos.utility.RegisterClient;
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

    private final DistributedLockProxyService distributedLockProxyService;
    private final RegisterClient registerClient;

    public AuthenticationEventListener(DistributedLockProxyService distributedLockProxyService, RegisterClient registerClient) {
        this.distributedLockProxyService = distributedLockProxyService;
        this.registerClient = registerClient;
    }

    @Async
    @EventListener
    public void onAuthenticationSuccess(InteractiveAuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthContext context)) {
            return;
        }

        DistributedLockDTO lock = new DistributedLockDTO(
                LockTypeEnum.ACCOUNT,
                context.getAccountId(),
                registerClient.getInstanceId(),
                Instant.now(),
                Instant.now().plus(1, ChronoUnit.HOURS)
        );

        distributedLockProxyService
                .createDistributedLock(lock)
                .doOnError(ex -> log.atError().log("Failed to create distributed lock", ex))
                .subscribe();
    }

    @Async
    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthContext context)) {
            return;
        }

        distributedLockProxyService
                .deleteByLockTypeAndTargetId(LockTypeEnum.ACCOUNT, context.getAccountId())
                .doOnError(ex -> log.atError().log("Failed to delete distributed lock", ex))
                .subscribe();
    }
}