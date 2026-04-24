package de.fhdw.vendix.commons.spring.starter.autoconfigure.security;

import de.fhdw.vendix.commons.spring.app.context.app.AppContext;
import de.fhdw.vendix.commons.spring.app.context.app.DefaultAppContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration(before = SecurityContextAutoConfiguration.class)
public class SecurityLifecycleAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AppContext appContext(Environment environment) {
        return new DefaultAppContext(environment);
    }
}