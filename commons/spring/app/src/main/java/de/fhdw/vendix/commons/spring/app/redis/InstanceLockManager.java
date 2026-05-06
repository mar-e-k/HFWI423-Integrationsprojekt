package de.fhdw.vendix.commons.spring.app.redis;

import de.fhdw.vendix.commons.spring.app.context.DomainContextEvent;
import de.fhdw.vendix.commons.spring.app.context.register.RegisterContextInitializedEvent;
import de.fhdw.vendix.commons.spring.app.context.store.StoreContextInitializedEvent;
import de.fhdw.vendix.commons.spring.data.caching.RedissonKey;
import jakarta.annotation.PreDestroy;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class InstanceLockManager {

    private static final Logger log = LoggerFactory.getLogger(InstanceLockManager.class);

    private final RedissonClient redissonClient;

    public InstanceLockManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void onContextInitialized(DomainContextEvent event) {
        Long domainId = event.getDomain().id();
        if (domainId == null || domainId <= 0) {
            log.atError().log("Cannot lock domain entity: invalid id={}", domainId);
            return;
        }

        RedissonKey keyType = switch (event) {
            case RegisterContextInitializedEvent r -> RedissonKey.REGISTER_LOCK;
            case StoreContextInitializedEvent s -> RedissonKey.STORE_LOCK;
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };

        String lockKey = keyType.lockKey(domainId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(0, -1, TimeUnit.SECONDS);

            if (acquired) {
                redissonClient.getSet(keyType.setKey()).add(domainId);
                log.atInfo().log("Lock acquired: {} (domainId={})", lockKey, domainId);
            } else {
                log.atWarn().log("Lock already held: {} (domainId={})", lockKey, domainId);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.atError().log("Interrupted while acquiring lock {}", lockKey, e);
        }
    }

    @PreDestroy
    public void releaseAllLocks() {
        log.atInfo().log("Releasing all locks on shutdown...");
        releaseLocksForType(RedissonKey.REGISTER_LOCK);
        releaseLocksForType(RedissonKey.STORE_LOCK);
        log.atInfo().log("Successfully released locks");
    }

    private void releaseLocksForType(RedissonKey keyType) {
        Set<Long> ids = redissonClient.getSet(keyType.setKey());

        for (Long id : ids) {
            String lockKey = keyType.lockKey(id);
            RLock lock = redissonClient.getLock(lockKey);

            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    redissonClient.getSet(keyType.setKey()).remove(id);
                    log.atInfo().log("Released lock: {}", lockKey);
                }
            } catch (Exception e) {
                log.atError().log("Failed to release lock {}", lockKey, e);
            }
        }
    }
}