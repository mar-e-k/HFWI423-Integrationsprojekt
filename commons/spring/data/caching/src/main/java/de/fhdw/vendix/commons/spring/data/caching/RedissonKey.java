package de.fhdw.vendix.commons.spring.data.caching;

public enum RedissonKey {

    STORE_LOCK("store-id:%d"),
    REGISTER_LOCK("register-id:%d");

    private final String pattern;

    RedissonKey(String pattern) {
        this.pattern = pattern;
    }

    public String format(Object... args) {
        return String.format(this.pattern, args);
    }
}