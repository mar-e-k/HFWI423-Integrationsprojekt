package de.fhdw.vendix.commons.spring.security.jwt;

public enum JwtClaims {
    ROLES("roles"),
    STORE("store"),
    REGISTER("register"),;

    private final String claim;

    JwtClaims(String claim) {
        this.claim = claim;
    }

    public String claim() {
        return claim;
    }
}