package de.fhdw.vendix.commons.spring.starter.autoconfigure.security;

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
    public JwtValidator jwtValidator() {
        return new DefaultJwtValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtService jwtService(JwtProperties jwtProperties) {
        return new DefaultJwtService(jwtProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public de.fhdw.vendix.commons.spring.web.filter.JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, JwtValidator jwtValidator, AccountProxyService accountProxyService) {
        return new de.fhdw.vendix.commons.spring.web.filter.JwtAuthenticationFilter(jwtService, jwtValidator, accountProxyService);
    }
}