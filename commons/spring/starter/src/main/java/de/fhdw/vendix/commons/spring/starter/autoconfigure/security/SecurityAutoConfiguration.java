package de.fhdw.vendix.commons.spring.starter.autoconfigure.security;

import de.fhdw.vendix.commons.spring.security.authentication.AuthenticationService;
import de.fhdw.vendix.commons.spring.security.authorization.AuthorizationService;
import de.fhdw.vendix.commons.spring.security.context.app.AppContext;
import de.fhdw.vendix.commons.spring.security.listener.ApplicationEventListener;
import de.fhdw.vendix.commons.spring.security.listener.AuthenticationEventListener;
import de.fhdw.vendix.commons.spring.security.listener.AuthenticationLifecycleHandler;
import de.fhdw.vendix.commons.spring.security.listener.DefaultAuthenticationLifecycleHandler;
import de.fhdw.vendix.commons.spring.security.user_details.DefaultUserDetailsService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@AutoConfiguration(after = SecurityContextAutoConfiguration.class)
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public UserDetailsService userDetailsService(AuthenticationService authenticationPort, AuthorizationService authorizationPort) {
        return new DefaultUserDetailsService(authenticationPort, authorizationPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
                return Optional.of(userDetails.getUsername());
            }
            return Optional.of("unknown");
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationLifecycleHandler authenticationLifecycleHandler(AppContext appContext, AuthorizationService authorizationPort) {
        return new DefaultAuthenticationLifecycleHandler(appContext, authorizationPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationEventListener authenticationEventListener(AuthenticationLifecycleHandler authenticationLifecycleHandler) {
        return new AuthenticationEventListener(authenticationLifecycleHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationEventListener applicationEventListener(AppContext appContext, AuthenticationLifecycleHandler authenticationLifecycleHandler) {
        return new ApplicationEventListener(appContext, authenticationLifecycleHandler);
    }
}