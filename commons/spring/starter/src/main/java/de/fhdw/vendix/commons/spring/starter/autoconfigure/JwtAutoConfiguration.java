package de.fhdw.vendix.commons.spring.starter.autoconfigure;

import de.fhdw.vendix.security.api.authentication.AuthenticationService;
import de.fhdw.vendix.security.api.context.AppContext;
import de.fhdw.vendix.security.api.context.RegisterContext;
import de.fhdw.vendix.security.api.context.StoreContext;
import de.fhdw.vendix.security.api.jwt.JwtProperties;
import de.fhdw.vendix.security.api.jwt.JwtService;
import de.fhdw.vendix.commons.security.core.jwt.DefaultJwtService;
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
    public JwtService jwtService(AppContext appContext, StoreContext storeContext, RegisterContext registerContext, JwtProperties jwtProperties) {
        return new DefaultJwtService(appContext, storeContext, registerContext, jwtProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, AuthenticationService authenticationPort) {
        return new JwtAuthenticationFilter(jwtService, authenticationPort);
    }
}