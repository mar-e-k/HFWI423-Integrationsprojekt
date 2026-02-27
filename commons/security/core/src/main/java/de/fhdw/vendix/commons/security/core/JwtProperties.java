package de.fhdw.vendix.commons.security.core;

import java.time.Duration;

public interface JwtProperties {
    String privateKey();
    Duration expiration();
}