package de.fhdw.commons.ui.utlity.config;


import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("vaadin.application")
public class VaadinProperties {

    @NotNull(message = "Error Overlay Duration cannot be null")
    private Duration errorOverlayDuration = Duration.ofSeconds(10);

    private boolean enableErrorOverlay = true;

    public VaadinProperties() {}

    public Duration getErrorOverlayDuration() {
        return errorOverlayDuration;
    }

    public void setErrorOverlayDuration(Duration errorOverlayDuration) {
        this.errorOverlayDuration = errorOverlayDuration;
    }

    public boolean isEnableErrorOverlay() {
        return enableErrorOverlay;
    }

    public void setEnableErrorOverlay(boolean enableErrorOverlay) {
        this.enableErrorOverlay = enableErrorOverlay;
    }
}