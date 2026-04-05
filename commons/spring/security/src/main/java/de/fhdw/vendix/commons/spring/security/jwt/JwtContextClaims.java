package de.fhdw.vendix.commons.spring.security.jwt;

public enum JwtContextClaims {
    ROLES("roles"),
    STORE("store"),
    REGISTER("register"),;

    private final String claim;

    JwtContextClaims(String claim) {
        this.claim = claim;
    }

    public String claim() {
        return claim;
    }
}