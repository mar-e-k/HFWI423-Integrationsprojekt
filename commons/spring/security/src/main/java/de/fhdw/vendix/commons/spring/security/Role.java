package de.fhdw.vendix.commons.spring.security;

public enum Role {
    CASHIER(Constants.CASHIER),
    ADMIN(Constants.ADMIN);

    private final String name;

    Role(String name) {
        this.name = name;
    }

    public static class Constants {
        public static final String CASHIER = "CASHIER";
        public static final String ADMIN = "ADMIN";
    }
}