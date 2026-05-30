package de.fhdw.vendix.store.app.config;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Optional;

@Configuration
@EnableAsync
@EnableJpaAuditing
@EnableScheduling
class SpringConfig {

    private final AuthenticationContext authenticationContext;

    public SpringConfig(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> authenticationContext.getPrincipalName()
                .filter(name -> !name.isBlank())
                .or(() -> Optional.of("system"));
    }
}