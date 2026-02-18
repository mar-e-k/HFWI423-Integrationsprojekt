package de.fhdw.vendix.commons.security.jwt;

import java.time.Duration;

public interface JwtProperties {
    String privateKey();
    Duration expiration();
}