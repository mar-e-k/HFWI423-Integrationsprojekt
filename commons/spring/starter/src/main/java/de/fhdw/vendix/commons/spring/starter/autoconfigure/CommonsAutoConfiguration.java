package de.fhdw.vendix.commons.spring.starter.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.time.Clock;
import java.time.ZoneOffset;

@AutoConfiguration
@ComponentScan(basePackages = "de.fhdw.vendix.commons.spring.core.mapper.dto") // MapStruct Impl classes in /target
public class CommonsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Clock clock() {
        return Clock.system(ZoneOffset.UTC);
    }
}