package de.fhdw.commons.security.utility.config;

import de.fhdw.commons.security.utility.security.jwt.JwtService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {

    public JwtAutoConfiguration() {}

    @Bean
    @ConditionalOnMissingBean(JwtService.class)
    public JwtService jwtService(JwtProperties properties) {
        return new JwtService(properties);
    }
}