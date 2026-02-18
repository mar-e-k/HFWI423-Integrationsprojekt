package de.fhdw.vendix.commons.spring.autoconfigure;

import de.fhdw.vendix.commons.security.jwt.JwtAuthenticationFilter;
import de.fhdw.vendix.commons.security.jwt.JwtProperties;
import de.fhdw.vendix.commons.security.jwt.JwtService;
import de.fhdw.vendix.commons.security.jwt.JwtServiceImpl;
import de.fhdw.vendix.commons.spring.properties.JwtPropertiesConfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@AutoConfiguration
@EnableConfigurationProperties(JwtPropertiesConfiguration.class)
public class JwtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JwtService.class)
    public JwtService jwtService(JwtProperties properties) {
        return new JwtServiceImpl(properties);
    }

    // TODO: placeholder
    @Bean
    @ConditionalOnMissingBean(JwtAuthenticationFilter.class)
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter() {
            @Override
            public Authentication resolveAuthentication(String token) throws AuthenticationException {
                return null;
            }
        };
    }
}