package de.fhdw.vendix.commons.spring.starter.autoconfigure.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
public class DatabasePropertiesAutoConfiguration {

    @Configuration
    @PropertySource("classpath:config/database/defaults.properties")
    static class DatabaseDefaultConfig {}

    @Configuration
    @Profile("prod")
    @PropertySource("classpath:config/database/prod.properties")
    static class DatabaseProdConfig {}

    @Configuration
    @Profile("test")
    @PropertySource("classpath:config/database/test.properties")
    static class DatabaseTestConfig {}

    @Configuration
    @Profile("dev")
    @PropertySource("classpath:config/database/dev.properties")
    static class DatabaseDevConfig {}

    @Configuration
    @Profile("local")
    @PropertySource("classpath:config/database/local.properties")
    static class DatabaseLocalConfig {}
}