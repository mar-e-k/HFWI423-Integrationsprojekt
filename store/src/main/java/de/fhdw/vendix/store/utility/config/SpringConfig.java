package de.fhdw.vendix.store.utility.config;

import de.fhdw.vendix.commons.security.auth.AuthContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Optional;

@EnableAsync
@EnableScheduling
@EnableJpaAuditing
@Configuration
public class SpringConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> AuthContextHolder.current()
                .map(AuthContext::getName)
                .or(() -> Optional.of("system"));
    }
}