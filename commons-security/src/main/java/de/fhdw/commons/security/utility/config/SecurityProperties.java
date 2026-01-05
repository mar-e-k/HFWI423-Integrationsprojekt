package de.fhdw.commons.security.utility.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.security")
public final class SecurityProperties {

    private boolean securityEnabled = true;

    public SecurityProperties() {}

    public boolean isSecurityEnabled() {
        return securityEnabled;
    }

    public void setSecurityEnabled(boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }
}