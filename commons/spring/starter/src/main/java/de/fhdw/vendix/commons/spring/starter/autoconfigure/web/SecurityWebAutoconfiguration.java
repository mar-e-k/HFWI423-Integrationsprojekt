package de.fhdw.vendix.commons.spring.starter.autoconfigure.web;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import de.fhdw.vendix.commons.spring.security.jwt.JwtAuthenticationFilter;
import de.fhdw.vendix.commons.spring.starter.autoconfigure.security.SecurityAutoConfiguration;
import de.fhdw.vendix.commons.spring.web.handler.DefaultAuthenticationFailureHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@AutoConfiguration(after = SecurityAutoConfiguration.class)
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
    public SecurityFilterChain apiSecurity(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) {
        return http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterAt(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain vaadinSecurity(HttpSecurity http, AuthenticationFailureHandler vendixAuthenticationFailureHandler) {
        return http
                .with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer
                        .loginView("/login")
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureHandler(vendixAuthenticationFailureHandler))
                .build();
    }
}