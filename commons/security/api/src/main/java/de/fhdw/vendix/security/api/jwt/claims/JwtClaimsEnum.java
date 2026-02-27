package de.fhdw.vendix.security.api.jwt.claims;

public enum JwtClaimsEnum {
    SUBJECT("sub"),
    ROLES("roles");

    private final String claim;

    JwtClaimsEnum(String claim) {
        this.claim = claim;
    }

    public String claim() {
        return claim;
    }
}