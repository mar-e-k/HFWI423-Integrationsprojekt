package de.fhdw.vendix.commons.security.jwt.claims;

public enum JwtClaimsEnum {
    SUBJECT("sub"),
    ACCOUNT_ROLES("roles"),
    STORE_ID("store"),
    REGISTER_ID("register");

    private final String claim;

    JwtClaimsEnum(String claim) {
        this.claim = claim;
    }

    public String claim() {
        return claim;
    }
}