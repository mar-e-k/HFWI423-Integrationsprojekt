package de.fhdw.vendix.commons.spring.starter.autoconfigure.app;

import de.fhdw.vendix.commons.spring.app.consul.ConsulClientUpdater;
import de.fhdw.vendix.commons.spring.app.context.app.AppContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ConsulAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConsulClientUpdater consulClientUpdater(AppContext appContext, ConsulClient consulClient) {
        return new ConsulClientUpdater(appContext, consulClient);
    }
}