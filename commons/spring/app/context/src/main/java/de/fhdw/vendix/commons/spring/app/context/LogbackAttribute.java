package de.fhdw.vendix.commons.spring.app.context;

public enum LogbackAttribute {
    SERVICE_NAME("__service_name__"),
    HOST("__host__"),
    INSTANCE("__instance__"),
    DOMAIN_TYPE("__domain_type__"),
    DOMAIN_ID("__domain_id__");

    private final String keyName;
    LogbackAttribute(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyName() {
        return keyName;
    }
}