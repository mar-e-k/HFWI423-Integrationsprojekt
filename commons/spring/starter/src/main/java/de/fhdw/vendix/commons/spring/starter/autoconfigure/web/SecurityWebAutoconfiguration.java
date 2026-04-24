package de.fhdw.vendix.commons.spring.starter.autoconfigure.web;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import de.fhdw.vendix.commons.spring.app.lifecycle.authentication.DefaultAuthenticationFailureHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@AutoConfiguration
public class SecurityWebAutoconfiguration {

    @Bean
    public AuthenticationFailureHandler vendixAuthenticationFailureHandler() {
        return new DefaultAuthenticationFailureHandler();
    }

    @Bean
    @Order(0)
    public SecurityFilterChain prometheusSecurity(HttpSecurity http) {
        return http
                .securityMatcher("/prometheus/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurity(HttpSecurity http) {
        return http
                .securityMatcher("/actuator/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurity(HttpSecurity http) {
        return http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults())
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain vaadinSecurity(HttpSecurity http) {
        return http
                .with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer
                        .oauth2LoginPage(
                                "/oauth2/authorization/keycloak",
                                "{baseUrl}/session-ended"
                        )
                )
                .build();
    }
}