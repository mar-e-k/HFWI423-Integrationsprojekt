package de.fhdw.vendix.commons.spring.app.bundle.redis;

import de.fhdw.vendix.commons.spring.app.context.DomainContextEvent;
import de.fhdw.vendix.commons.spring.app.context.register.RegisterContextInitializedEvent;
import de.fhdw.vendix.commons.spring.app.context.store.StoreContextInitializedEvent;
import de.fhdw.vendix.commons.spring.data.caching.RedissonKey;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.concurrent.TimeUnit;

public final class DefaultRedisInstanceContextListener implements RedisInstanceContextListener {

    private static final Logger log = LoggerFactory.getLogger(DefaultRedisInstanceContextListener.class);

    private final RedissonClient redissonClient;

    public DefaultRedisInstanceContextListener(RedissonClient redissonClient) {
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
            case RegisterContextInitializedEvent _ -> RedissonKey.REGISTER_LOCK;
            case StoreContextInitializedEvent _ -> RedissonKey.STORE_LOCK;
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };

        String lockKey = keyType.toIdentifier(String.valueOf(domainId));
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(0, TimeUnit.SECONDS);

            if (acquired) {
                log.atInfo().log("Lock acquired: {} (domainId={})", lockKey, domainId);
            } else {
                log.atWarn().log("Lock already held: {} (domainId={})", lockKey, domainId);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.atError().log("Interrupted while acquiring lock {}", lockKey, e);
        }
    }
}