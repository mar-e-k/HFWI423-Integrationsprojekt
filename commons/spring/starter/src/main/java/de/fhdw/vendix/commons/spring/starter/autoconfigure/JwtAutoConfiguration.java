package de.fhdw.vendix.commons.spring.starter.autoconfigure;

import de.fhdw.vendix.security.api.authentication.AuthenticationQueryApi;
import de.fhdw.vendix.security.api.jwt.JwtProperties;
import de.fhdw.vendix.security.api.jwt.JwtService;
import de.fhdw.vendix.commons.security.core.JwtServiceImpl;
import de.fhdw.vendix.commons.security.spring.filter.JwtAuthenticationFilter;
import de.fhdw.vendix.commons.spring.starter.properties.JwtPropertiesConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(JwtPropertiesConfiguration.class)
public class JwtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtService jwtService(JwtProperties jwtProperties) {
        return new JwtServiceImpl(jwtProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, AuthenticationQueryApi authenticationQueryApi) {
        return new JwtAuthenticationFilter(jwtService, authenticationQueryApi);
    }
}