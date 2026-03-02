package de.fhdw.vendix.commons.spring.starter.autoconfigure;

import com.vaadin.flow.server.VaadinServiceInitListener;
import de.fhdw.vendix.commons.spring.starter.properties.VaadinPropertiesConfiguration;
import de.fhdw.vendix.commons.ui.vaadin.layout.GlobalUiExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(VaadinPropertiesConfiguration.class)
public class VaadinAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(VaadinServiceInitListener.class)
    public VaadinServiceInitListener vaadinServiceInitListener(VaadinPropertiesConfiguration vaadinPropertiesConfiguration) {
        return event -> {
            if (!vaadinPropertiesConfiguration.enableErrorOverlay()) {
                return;
            }
            event.getSource().addSessionInitListener(sessionEvent ->
                    sessionEvent.getSession().setErrorHandler(
                            new GlobalUiExceptionHandler(vaadinPropertiesConfiguration.errorOverlayDuration())
                    )
            );
        };
    }
}