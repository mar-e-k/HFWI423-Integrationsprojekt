package de.fhdw.vendix.commons.spring.starter.autoconfigure;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.api.domain.lock.port.LockCommandPort;
import de.fhdw.vendix.commons.api.domain.lock.port.LockQueryPort;
import de.fhdw.vendix.commons.security.core.DefaultAuthenticationLifecycleHandler;
import de.fhdw.vendix.commons.security.spring.DefaultAppContext;
import de.fhdw.vendix.commons.security.spring.authentication.AuthenticationHandler;
import de.fhdw.vendix.commons.security.spring.authentication.DefaultAuthenticationHandler;
import de.fhdw.vendix.commons.security.spring.authentication.DefaultAuthenticationProvider;
import de.fhdw.vendix.commons.security.spring.JwtAuthenticationFilter;
import de.fhdw.vendix.commons.security.spring.listener.ApplicationEventListener;
import de.fhdw.vendix.commons.security.spring.listener.AuthenticationEventListener;
import de.fhdw.vendix.security.api.authentication.AppContext;
import de.fhdw.vendix.commons.security.core.AuthenticationLifecycleHandler;
import de.fhdw.vendix.security.api.authentication.AuthContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Optional;

@AutoConfiguration
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationProvider authenticationProvider(AccountQueryPort accountQueryPort, LockQueryPort lockQueryPort, PasswordEncoder passwordEncoder) {
        return new DefaultAuthenticationProvider(accountQueryPort, lockQueryPort, passwordEncoder);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationHandler authenticationHandler(ApplicationEventPublisher applicationEventPublisher, AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository) {
        return new DefaultAuthenticationHandler(applicationEventPublisher, authenticationManager, securityContextRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof AuthContext authContext) {
                return Optional.of(authContext.accountUsername());
            }
            return Optional.of("unknown");
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public AppContext appContext(Environment environment) {
        return new DefaultAppContext(environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationLifecycleHandler authenticationLifecycleHandler(AppContext appContext, LockCommandPort commandPort) {
        return new DefaultAuthenticationLifecycleHandler(appContext, commandPort);
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

    @Bean
    @Order(0)
    public SecurityFilterChain apiSecurity(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) {
        return http
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain vaadinSecurity(HttpSecurity http) {
        return http
                .with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer
                        .loginView("/login")
                )
                .build();
    }
}