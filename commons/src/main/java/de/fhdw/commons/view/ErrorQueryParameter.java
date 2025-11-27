package de.fhdw.commons.view;

public enum ErrorQueryParameter {
    ACCESS_DENIED("access-denied"),
    ROLES_MISSING("roles-missing"),
    LOGIN_REQUIRED("login-required");

    private final String value;

    ErrorQueryParameter(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}