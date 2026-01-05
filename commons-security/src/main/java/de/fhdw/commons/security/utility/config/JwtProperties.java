package de.fhdw.commons.security.utility.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("spring.security.jwt")
public final class JwtProperties {

    @NotBlank(message = "Private key must not be blank")
    @Size(min = 32, message = "Private key length must be >= 32")
    private String privateKey = "SuperSecretApiKeyForJwtEncryptionAndDecryption"; // TODO: this needs to be in a vault or similar. Not secure enough if manually typed

    @NotNull(message = "Expiration cannot be null")
    private Duration expiration = Duration.ofMinutes(5);

    public JwtProperties() {}

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public Duration getExpiration() {
        return expiration;
    }

    public void setExpiration(Duration expiration) {
        this.expiration = expiration;
    }
}