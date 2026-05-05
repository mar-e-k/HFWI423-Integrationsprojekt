package de.fhdw.vendix.commons.spring.starter.autoconfigure.web;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import de.fhdw.vendix.commons.spring.security.KeycloakJwtAuthenticationConverter;
import de.fhdw.vendix.commons.spring.web.filter.RequestRateFilter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@AutoConfiguration
public class SecurityWebAutoconfiguration {

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
    public SecurityFilterChain apiSecurity(HttpSecurity http, RedissonClient redissonClient) {
        return http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter()))
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilter(new RequestRateFilter(redissonClient))
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