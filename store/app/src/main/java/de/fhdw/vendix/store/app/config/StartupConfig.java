package de.fhdw.vendix.store.app.config;

import de.fhdw.vendix.store.core.event.initializer.CleanupStoreHostsAndLocksInitializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupConfig {

    @Bean
    @ConditionalOnProperty(
            prefix = "",
            name = "",
            havingValue = "true",
            matchIfMissing = false
    )
    CleanupStoreHostsAndLocksInitializer cleanupStoreHostsAndLocksInitializer() {
        return new CleanupStoreHostsAndLocksInitializer();
    }
}