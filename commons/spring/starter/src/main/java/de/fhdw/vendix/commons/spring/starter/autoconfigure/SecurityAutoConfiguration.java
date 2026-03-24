package de.fhdw.vendix.commons.spring.starter.autoconfigure;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import de.fhdw.vendix.security.api.authentication.AuthenticationQueryApi;
import de.fhdw.vendix.security.api.authorization.AuthorizationCommandApi;
import de.fhdw.vendix.security.api.authorization.AuthorizationQueryApi;
import de.fhdw.vendix.commons.security.spring.authentication.DefaultAuthenticationLifecycleHandler;
import de.fhdw.vendix.commons.security.spring.context.DefaultAppContext;
import de.fhdw.vendix.commons.security.spring.authentication.DefaultUserDetailsService;
import de.fhdw.vendix.commons.security.spring.filter.JwtAuthenticationFilter;
import de.fhdw.vendix.commons.security.spring.listener.ApplicationEventListener;
import de.fhdw.vendix.commons.security.spring.listener.AuthenticationEventListener;
import de.fhdw.vendix.security.api.context.AppContext;
import de.fhdw.vendix.security.api.AuthenticationLifecycleHandler;
import de.fhdw.vendix.security.api.context.AuthContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public UserDetailsService userDetailsService(AuthenticationQueryApi authenticationQueryApi, AuthorizationQueryApi authorizationQueryApi) {
        return new DefaultUserDetailsService(authenticationQueryApi, authorizationQueryApi);
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
    public AuthenticationLifecycleHandler authenticationLifecycleHandler(AppContext appContext, AuthorizationCommandApi authorizationCommandApi) {
        return new DefaultAuthenticationLifecycleHandler(appContext, authorizationCommandApi);
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