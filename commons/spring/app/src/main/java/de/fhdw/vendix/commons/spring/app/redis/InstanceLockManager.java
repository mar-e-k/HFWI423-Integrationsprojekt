package de.fhdw.vendix.commons.spring.app.redis;

import de.fhdw.vendix.commons.spring.app.context.DomainContextEvent;
import de.fhdw.vendix.commons.spring.app.context.register.RegisterContextInitializedEvent;
import de.fhdw.vendix.commons.spring.app.context.store.StoreContextInitializedEvent;
import jakarta.annotation.Nullable;
import jakarta.annotation.PreDestroy;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.concurrent.TimeUnit;

public final class InstanceLockManager {

    private static final Logger log = LoggerFactory.getLogger(InstanceLockManager.class);

    @Nullable
    private RLock currentLock;

    private final RedissonClient redissonClient;

    public InstanceLockManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void onRegisterContextInitialized(DomainContextEvent event) {
        Long domainId = event.getDomain().id();
        if (domainId == null || domainId <= 0) {
            log.atError().log("Cannot lock domain-entity as domain id is null or negative");
            return;
        }

        String lockKey = switch (event) {
            case RegisterContextInitializedEvent r -> "register-id:" + domainId;
            case StoreContextInitializedEvent s -> "store-id" + domainId;
            default -> throw new IllegalStateException("Unexpected value: " + event);
        };
        RLock currentLock = redissonClient.getLock(lockKey);

        try {
            if (currentLock.tryLock(0, -1, TimeUnit.SECONDS)) {
                log.atInfo().log("Successfully acquired lock for register ID: {}", domainId);
            } else {
                log.atWarn().log("Failed to acquire lock for register ID: {} (Already taken)", domainId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.atError().log("Lock acquisition was interrupted", e);
        }
    }

    @PreDestroy
    public void releaseAllLocks() {
        log.atInfo().log("Releasing register lock on application shutdown...");
        if (currentLock != null && currentLock.isHeldByCurrentThread()) {
            currentLock.unlock();
            log.atInfo().log("Successfully released register lock");
        } else {
            log.atWarn().log("Cannot release register lock as none is set");
        }
    }
}