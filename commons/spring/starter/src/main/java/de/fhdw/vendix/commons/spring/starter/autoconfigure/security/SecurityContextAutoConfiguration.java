package de.fhdw.vendix.commons.spring.starter.autoconfigure.security;

import de.fhdw.vendix.commons.spring.security.context.app.AppContext;
import de.fhdw.vendix.commons.spring.security.context.app.DefaultAppContext;
import de.fhdw.vendix.commons.spring.security.context.register.DefaultRegisterContext;
import de.fhdw.vendix.commons.spring.security.context.register.RegisterContext;
import de.fhdw.vendix.commons.spring.security.context.store.DefaultStoreContext;
import de.fhdw.vendix.commons.spring.security.context.store.StoreContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
public class SecurityContextAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AppContext appContext(Environment environment) {
        return new DefaultAppContext(environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public StoreContext storeContext(ApplicationEventPublisher applicationEventPublisher) {
        return new DefaultStoreContext(applicationEventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public RegisterContext registerContext(ApplicationEventPublisher applicationEventPublisher) {
        return new DefaultRegisterContext(applicationEventPublisher);
    }
}