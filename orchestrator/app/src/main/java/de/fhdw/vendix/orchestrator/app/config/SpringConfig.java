package de.fhdw.vendix.orchestrator.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableAsync
@EnableJpaAuditing
@EnableScheduling
public class SpringConfig {

    public SpringConfig() {}

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof String username) {
                    return Optional.of(username);
                }
                if (principal instanceof UUID systemId) {
                    return Optional.of(systemId.toString());
                }
            }
            return Optional.of("unknown");
        };
    }
}