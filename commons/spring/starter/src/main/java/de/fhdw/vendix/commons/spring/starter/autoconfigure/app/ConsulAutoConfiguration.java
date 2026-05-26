package de.fhdw.vendix.commons.spring.starter.autoconfigure.app;

import de.fhdw.vendix.commons.spring.app.bundle.consul.ConsulInstanceContextListener;
import de.fhdw.vendix.commons.spring.app.bundle.consul.DefaultConsulInstanceContextListener;
import de.fhdw.vendix.commons.spring.app.context.system.SystemContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ConsulAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConsulInstanceContextListener consulClientUpdater(
            ConsulServiceRegistry registry,
            ConsulDiscoveryProperties properties,
            SystemContext systemContext
    ) {
        return new DefaultConsulInstanceContextListener(registry, properties, systemContext);
    }
}