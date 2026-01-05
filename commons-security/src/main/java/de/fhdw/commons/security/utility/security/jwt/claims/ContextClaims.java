package de.fhdw.commons.security.utility.security.jwt.claims;

public record ContextClaims(
        Long storeId,
        Long registerId
) {
    public static ContextClaims empty() {
        return new ContextClaims(null, null);
    }

    public static ContextClaims context(Long storeId, Long registerId) {
        return new ContextClaims(storeId, registerId);
    }
}