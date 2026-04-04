package de.fhdw.vendix.commons.spring.security.jwt;

import java.time.Duration;

public interface JwtProperties {
    String privateKey();
    Duration expiration();
}