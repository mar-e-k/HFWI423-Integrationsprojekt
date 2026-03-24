package de.fhdw.vendix.security.api.jwt;

import java.time.Duration;

public interface JwtProperties {
    String privateKey();
    Duration expiration();
}