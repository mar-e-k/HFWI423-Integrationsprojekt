package de.fhdw.vendix.store.app.config;

import de.fhdw.vendix.store.core.caching.StoreRedissonCachingKey;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CachingConfig {

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();

        registerArticleCaches(config);

        return new RedissonSpringCacheManager(redissonClient, config);
    }

    private void registerArticleCaches(Map<String, CacheConfig> config) {
        CacheConfig articleConfig = new CacheConfig(10 * 60 * 1000, 5 * 60 * 1000);
        config.put(StoreRedissonCachingKey.Constants.ARTICLES, articleConfig);
        config.put(StoreRedissonCachingKey.Constants.ARTICLE_BY_ID, articleConfig);
        config.put(StoreRedissonCachingKey.Constants.ARTICLE_BY_GTIN, articleConfig);
    }
}