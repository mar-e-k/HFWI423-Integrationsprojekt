package de.fhdw.vendix.commons.spring.starter.autoconfigure.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
public class SecurityPropertiesAutoConfiguration {

    @Configuration
    @PropertySource("classpath:config/security/defaults.properties")
    static class SecurityDefaultConfig {}

    @Configuration
    @Profile("prod")
    @PropertySource("classpath:config/security/prod.properties")
    static class SecurityProdConfig {}

    @Configuration
    @Profile("test")
    @PropertySource("classpath:config/security/test.properties")
    static class SecurityTestConfig {}

    @Configuration
    @Profile("dev")
    @PropertySource("classpath:config/security/dev.properties")
    static class SecurityDevConfig {}

    @Configuration
    @Profile("local")
    @PropertySource("classpath:config/security/local.properties")
    static class SecurityLocalConfig {}
}