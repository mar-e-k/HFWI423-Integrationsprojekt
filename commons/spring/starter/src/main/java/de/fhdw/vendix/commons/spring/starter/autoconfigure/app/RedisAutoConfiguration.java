package de.fhdw.vendix.commons.spring.starter.autoconfigure.app;

import de.fhdw.vendix.commons.spring.app.bundle.redis.DefaultRedisInstanceContextListener;
import de.fhdw.vendix.commons.spring.app.bundle.redis.RedisInstanceContextListener;
import de.fhdw.vendix.commons.spring.data.caching.RedissonLockUtils;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class RedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedissonLockUtils redissonLockUtils(RedissonClient redissonClient) {
        return new RedissonLockUtils(redissonClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisInstanceContextListener instanceLockManager(RedissonClient redissonClient) {
        return new DefaultRedisInstanceContextListener(redissonClient);
    }
}