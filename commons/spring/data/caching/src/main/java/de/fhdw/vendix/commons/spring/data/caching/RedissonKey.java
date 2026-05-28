package de.fhdw.vendix.commons.spring.data.caching;

public enum RedissonKey {

    REGISTER_LOCK("register-id"),
    STORE_LOCK("store-id"),
    RATE_LIMIT("rate-limit");

    private final String key;

    RedissonKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String toGlobalIdentifier() {
        return key.concat(":*");
    }

    public String toIdentifier(String identifier) {
        return key.concat(":").concat(identifier);
    }
}