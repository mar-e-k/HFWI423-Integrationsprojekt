package de.fhdw.vendix.commons.spring.starter.autoconfigure;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.api.domain.lock.port.LockCommandPort;
import de.fhdw.vendix.commons.api.domain.lock.port.LockQueryPort;
import de.fhdw.vendix.commons.security.core.DefaultAuthenticationLifecycleHandler;
import de.fhdw.vendix.commons.security.spring.DefaultAppContext;
import de.fhdw.vendix.commons.security.spring.AuthContextHolder;
import de.fhdw.vendix.commons.security.spring.DefaultUserDetailsService;
import de.fhdw.vendix.commons.security.spring.JwtAuthenticationFilter;
import de.fhdw.vendix.commons.security.spring.DefaultClassAccessChecker;
import de.fhdw.vendix.commons.security.spring.listener.ApplicationEventListener;
import de.fhdw.vendix.commons.security.spring.listener.AuthenticationEventListener;
import de.fhdw.vendix.security.api.auth.AppContext;
import de.fhdw.vendix.security.api.auth.AuthContext;
import de.fhdw.vendix.security.api.auth.AuthenticationLifecycleHandler;
import de.fhdw.vendix.security.api.ui.ClassAccessChecker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
    public UserDetailsService userDetailsService(AccountQueryPort accountQueryPort, LockQueryPort lockQueryPort) {
        return new DefaultUserDetailsService(accountQueryPort, lockQueryPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditorAware<String> auditorAware() {
        return () -> AuthContextHolder.current()
                .map(AuthContext::accountUsername)
                .or(() -> Optional.of("unknown"));
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
    @ConditionalOnMissingBean
    public ClassAccessChecker classAccessChecker() {
        return new DefaultClassAccessChecker();
    }

    @Bean
    @Order(0)
    public SecurityFilterChain apiSecurity(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) {
        return http
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