package de.fhdw.vendix.commons.spring.starter.autoconfigure.app;

import de.fhdw.vendix.commons.spring.app.bundle.consul.ConsulInstanceContextListener;
import de.fhdw.vendix.commons.spring.app.bundle.consul.DefaultConsulInstanceContextListener;
import de.fhdw.vendix.commons.spring.app.context.system.SystemContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistrationCustomizer;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ConsulAutoConfiguration {

    @Bean
    public ConsulRegistrationCustomizer initialConsulRegistrationCustomizer(SystemContext systemContext) {
        return registration -> {
            String initialId = String.format("%s-0-%s",
                    systemContext.getApplicationName(),
                    systemContext.getInstanceUuid().toString());
            registration.getService().setId(initialId);
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsulInstanceContextListener consulInstanceContextListener(
            ConsulServiceRegistry registry,
            ConsulDiscoveryProperties properties,
            ConsulRegistration registration,
            SystemContext systemContext
    ) {
        return new DefaultConsulInstanceContextListener(registry, properties, registration, systemContext);
    }
}