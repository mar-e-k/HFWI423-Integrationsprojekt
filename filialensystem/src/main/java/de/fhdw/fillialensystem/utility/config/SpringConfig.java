package de.fhdw.fillialensystem.utility.config;

import de.fhdw.commons.security.utility.security.auth.AuthContext;
import de.fhdw.commons.security.utility.security.auth.AuthContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Optional;

@EnableAsync
@EnableScheduling
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "de.fhdw.fillialensystem.persistence.repository")
@Configuration
public class SpringConfig {

    public SpringConfig() {}

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> AuthContextHolder.current()
                .map(AuthContext::getName)
                .or(() -> Optional.of("system"));
    }
}