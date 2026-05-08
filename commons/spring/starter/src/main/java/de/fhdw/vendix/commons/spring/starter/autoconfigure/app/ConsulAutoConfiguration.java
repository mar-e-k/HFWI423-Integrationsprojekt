package de.fhdw.vendix.commons.spring.starter.autoconfigure.app;

import de.fhdw.vendix.commons.spring.app.consul.ConsulInstanceContextListener;
import de.fhdw.vendix.commons.spring.app.consul.DefaultConsulInstanceContextListener;
import de.fhdw.vendix.commons.spring.app.context.app.AppContext;
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
            AppContext appContext
    ) {
        return new DefaultConsulInstanceContextListener(registry, properties, appContext);
    }
}