package de.fhdw.vendix.commons.spring.starter.autoconfigure.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
public class MessagingPropertiesAutoConfiguration {

    @Configuration
    @PropertySource("classpath:config/messaging/defaults.properties")
    static class MessagingDefaultConfig {}

    @Configuration
    @Profile("prod")
    @PropertySource("classpath:config/messaging/prod.properties")
    static class MessagingProdConfig {}

    @Configuration
    @Profile("test")
    @PropertySource("classpath:config/messaging/test.properties")
    static class MessagingTestConfig {}

    @Configuration
    @Profile("dev")
    @PropertySource("classpath:config/messaging/dev.properties")
    static class MessagingDevConfig {}

    @Configuration
    @Profile("local")
    @PropertySource("classpath:config/messaging/local.properties")
    static class MessagingLocalConfig {}
}