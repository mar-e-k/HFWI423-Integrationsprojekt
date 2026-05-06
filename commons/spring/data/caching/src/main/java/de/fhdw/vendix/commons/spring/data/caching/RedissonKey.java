package de.fhdw.vendix.commons.spring.data.caching;

import org.jspecify.annotations.Nullable;

import java.util.IllegalFormatException;

public enum RedissonKey {

    REGISTER_LOCK("register-id:%d", "locks:register"),
    STORE_LOCK("store-id:%d", "locks:store"),
    RATE_LIMIT("rate-limit:%s", null);

    private final String lockPattern;

    @Nullable
    private final String setKey;

    RedissonKey(String lockPattern, @Nullable String setKey) {
        this.lockPattern = lockPattern;
        this.setKey = setKey;
    }

    public String lockKey(Object... args) throws IllegalFormatException {
        return String.format(lockPattern, args);
    }

    public String setKey() {
        if (setKey == null) {
            throw new IllegalStateException("No set defined for " + this.name());
        }
        return setKey;
    }
}