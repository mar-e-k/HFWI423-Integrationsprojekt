package de.fhdw.commons.ui.utlity.config;

import com.vaadin.flow.server.*;
import de.fhdw.commons.ui.utlity.GlobalUiExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.time.Duration;

@AutoConfiguration
@EnableConfigurationProperties(VaadinProperties.class)
public class VaadinAutoConfiguration implements VaadinServiceInitListener {

    private final Duration errorOverlayDuration;
    private final boolean enableErrorOverlay;

    public VaadinAutoConfiguration(VaadinProperties vaadinProperties) {
        this.enableErrorOverlay = vaadinProperties.isEnableErrorOverlay();
        this.errorOverlayDuration = vaadinProperties.getErrorOverlayDuration();
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        if (!enableErrorOverlay) {
            return;
        }

        event.getSource().addSessionInitListener(sessionEvent ->
                sessionEvent.getSession().setErrorHandler(new GlobalUiExceptionHandler(errorOverlayDuration))
        );
    }
}