package de.fhdw.commons.persistence.entity;

import org.springframework.security.core.GrantedAuthority;

public enum AccountRoleEnum implements GrantedAuthority {
    CASHIER,
    ADMIN;

    public static final String ROLE_CASHIER = "ROLE_CASHIER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}