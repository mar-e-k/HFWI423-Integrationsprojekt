package de.fhdw.vendix.store.app.config;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

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
        return () -> authenticationContext.getAuthenticatedUser(OidcUser.class)
                .map(oidcUser -> {
                    String username = oidcUser.getPreferredUsername();
                    return username != null ? username : oidcUser.getSubject();
                })
                .or(() -> authenticationContext.getPrincipalName()
                        .map(name -> {
                            if (name.isBlank()) {
                                return "unknown";
                            }
                            return name;
                        }))
                .or(() -> Optional.of("system"));
    }
}