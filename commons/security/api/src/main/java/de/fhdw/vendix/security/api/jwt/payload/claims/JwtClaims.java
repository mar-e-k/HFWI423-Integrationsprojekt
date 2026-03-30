package de.fhdw.vendix.security.api.jwt.payload.claims;

public enum JwtClaims {
    SUBJECT("sub"),
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