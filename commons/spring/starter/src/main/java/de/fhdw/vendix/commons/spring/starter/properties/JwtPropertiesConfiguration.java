package de.fhdw.vendix.commons.spring.starter.properties;

import de.fhdw.vendix.commons.spring.security.jwt.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.UUID;

@ConfigurationProperties(prefix = "vendix.security.jwt")
public record JwtPropertiesConfiguration(
        String privateKey,
        Duration expiration
) implements JwtProperties {

        private static final Logger log = LoggerFactory.getLogger(JwtPropertiesConfiguration.class);

        public JwtPropertiesConfiguration {
                if (privateKey == null) {
                        log.atWarn().log("Private key not set. Using random UUID. Consider configuring 'vendix.security.jwt.private-key' in .properties file.");
                        privateKey = UUID.randomUUID().toString();
                }
                expiration = expiration != null
                        ? expiration
                        : Duration.ofMinutes(5);
        }
}