package de.fhdw.vendix.security.api;

import java.time.Duration;

public interface JwtProperties {
    String privateKey();
    Duration expiration();
}