package de.fhdw.vendix.commons.spring.properties;

import de.fhdw.vendix.commons.security.jwt.JwtProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.UUID;

@ConfigurationProperties(prefix = "vendix.security.jwt")
@Validated
public record JwtPropertiesConfiguration(
        String privateKey,
        Duration expiration
) implements JwtProperties {
        public JwtPropertiesConfiguration {
                privateKey = privateKey != null
                        ? privateKey
                        : UUID.randomUUID().toString();
                expiration = expiration != null
                        ? expiration
                        : Duration.ofMinutes(5);
        }
}