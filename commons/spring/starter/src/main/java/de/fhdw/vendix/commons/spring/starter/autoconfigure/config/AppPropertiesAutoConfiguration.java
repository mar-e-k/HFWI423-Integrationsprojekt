package de.fhdw.vendix.commons.spring.starter.autoconfigure.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
public class AppPropertiesAutoConfiguration {

    @Configuration
    @PropertySource("classpath:config/app/defaults.properties")
    static class AppDefaultConfig {}

    @Configuration
    @Profile("prod")
    @PropertySource("classpath:config/app/prod.properties")
    static class AppProdConfig {}

    @Configuration
    @Profile("test")
    @PropertySource("classpath:config/app/test.properties")
    static class AppTestConfig {}

    @Configuration
    @Profile("dev")
    @PropertySource("classpath:config/app/dev.properties")
    static class AppDevConfig {}

    @Configuration
    @Profile("local")
    @PropertySource("classpath:config/app/local.properties")
    static class AppLocalConfig {}
}