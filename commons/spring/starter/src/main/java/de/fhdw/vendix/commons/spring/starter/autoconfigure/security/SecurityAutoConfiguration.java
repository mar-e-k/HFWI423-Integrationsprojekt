package de.fhdw.vendix.commons.spring.starter.autoconfigure.security;

import de.fhdw.vendix.commons.spring.app.user_details.DefaultUserDetailsService;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.AccountProxyService;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.DistributedLockProxyService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

@AutoConfiguration(after = SecurityContextAutoConfiguration.class)
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public UserDetailsService userDetailsService(AccountProxyService accountProxyService, DistributedLockProxyService distributedLockProxyService) {
        return new DefaultUserDetailsService(accountProxyService, distributedLockProxyService);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof String username) {  // your JWT filter sets account.username()
                    return Optional.of(username);
                }
                if (principal instanceof UUID systemId) {     // for SYSTEM role
                    return Optional.of(systemId.toString());
                }
            }
            return Optional.of("unknown");
        };
    }
}