package de.fhdw.vendix.store.core.caching;

import java.util.Optional;

public enum StoreRedissonCachingKey {

    ARTICLES(Constants.ARTICLES),
    ARTICLE_BY_ID(Constants.ARTICLE_BY_ID),
    ARTICLE_BY_GTIN(Constants.ARTICLE_BY_GTIN);

    private final String cacheName;

    StoreRedissonCachingKey(String cacheName) {
        this.cacheName = cacheName;
    }

    public String getCacheName() {
        return cacheName;
    }

    public static Optional<StoreRedissonCachingKey> from(String value) {
        try {
            return Optional.of(StoreRedissonCachingKey.valueOf(value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    /**
     * IMPORTANT: Annotation-safe constants required by Java compiler.
     * These are used inside @Cacheable, @CacheEvict, etc.
     */
    public static final class Constants {
        public static final String ARTICLES = "articles";
        public static final String ARTICLE_BY_ID = "article_by_id";
        public static final String ARTICLE_BY_GTIN = "article_by_gtin";

        private Constants() {}
    }
}