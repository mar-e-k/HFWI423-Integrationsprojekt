package de.fhdw.vendix.commons.spring.starter.autoconfigure;

import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.security.core.JwtProperties;
import de.fhdw.vendix.commons.security.core.JwtService;
import de.fhdw.vendix.commons.security.core.JwtServiceImpl;
import de.fhdw.vendix.commons.security.spring.JwtAuthenticationFilter;
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
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, AccountQueryPort accountQueryPort) {
        return new JwtAuthenticationFilter(jwtService, accountQueryPort);
    }
}