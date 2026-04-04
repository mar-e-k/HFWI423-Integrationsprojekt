package de.fhdw.vendix.commons.spring.starter.autoconfigure.web;

import de.fhdw.vendix.commons.spring.web.client.StoreClientHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StoreClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StoreClientHolder storeClientConfig() {
        return new StoreClientHolder();
    }
}