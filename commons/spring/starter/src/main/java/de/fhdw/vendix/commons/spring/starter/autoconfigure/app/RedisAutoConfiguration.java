package de.fhdw.vendix.commons.spring.starter.autoconfigure.app;

import de.fhdw.vendix.commons.spring.app.redis.InstanceLockManager;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class RedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public InstanceLockManager instanceLockManager(RedissonClient redissonClient) {
        return new InstanceLockManager(redissonClient);
    }
}