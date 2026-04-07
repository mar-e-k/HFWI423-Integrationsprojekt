package de.fhdw.vendix.commons.spring.starter.autoconfigure.security;

import de.fhdw.vendix.commons.spring.security.context.app.AppContext;
import de.fhdw.vendix.commons.spring.security.context.register.RegisterContext;
import de.fhdw.vendix.commons.spring.security.context.store.StoreContext;
import de.fhdw.vendix.commons.spring.security.jwt.*;
import de.fhdw.vendix.commons.spring.starter.properties.JwtPropertiesConfiguration;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.AccountProxyService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(JwtPropertiesConfiguration.class)
public class JwtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtValidator jwtValidator(AppContext appContext) {
        return new DefaultJwtValidator(appContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtService jwtService(AppContext appContext, StoreContext storeContext, RegisterContext registerContext, JwtProperties jwtProperties) {
        return new DefaultJwtService(appContext, storeContext, registerContext, jwtProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, JwtValidator jwtValidator, AccountProxyService accountProxyService) {
        return new JwtAuthenticationFilter(jwtService, jwtValidator, accountProxyService);
    }
}