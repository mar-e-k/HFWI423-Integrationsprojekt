package de.fhdw.vendix.commons.spring.properties;

import de.fhdw.vendix.commons.security.jwt.JwtProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.UUID;

@ConfigurationProperties(prefix = "vendix.security.jwt")
@Validated
public record JwtPropertiesConfiguration(
        @NotBlank
        @Size(min = 32)
        String privateKey,
        @NotNull
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