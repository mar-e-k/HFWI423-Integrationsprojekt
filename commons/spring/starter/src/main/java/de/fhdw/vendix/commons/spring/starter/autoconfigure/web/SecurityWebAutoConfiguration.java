package de.fhdw.vendix.commons.spring.starter.autoconfigure.web;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import de.fhdw.vendix.commons.spring.security.keycloak.KeycloakClientProperties;
import de.fhdw.vendix.commons.spring.security.keycloak.KeycloakJwtAuthenticationConverter;
import de.fhdw.vendix.commons.spring.security.keycloak.KeycloakOidcUserService;
import de.fhdw.vendix.commons.spring.web.core.config.RequestRateProperties;
import de.fhdw.vendix.commons.spring.web.core.filter.RequestRateFilter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@AutoConfiguration(after = KeycloakWebAutoConfiguration.class)
@EnableConfigurationProperties(RequestRateProperties.class)
public class SecurityWebAutoConfiguration {

    @Bean
    public RequestRateFilter requestRateFilter(RedissonClient redissonClient, RequestRateProperties requestRateProperties) {
        return new RequestRateFilter(redissonClient, requestRateProperties);
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
    public SecurityFilterChain docsSecurity(HttpSecurity http) {
        return http
                .securityMatcher("/docs/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain apiSecurity(
            HttpSecurity http,
            RequestRateFilter requestRateFilter,
            KeycloakJwtAuthenticationConverter jwtConverter
    ) {
        return http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter))
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterAfter(requestRateFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Order(4)
    public SecurityFilterChain vaadinSecurity(
            HttpSecurity http,
            KeycloakOidcUserService oidcUserService,
            KeycloakClientProperties clientProperties
    ) {
        return http
                .with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer
                        .oauth2LoginPage(
                                "/oauth2/authorization/" + clientProperties.webName(),
                                "{baseUrl}/session-ended"
                        )
                )
                .oauth2Login(oauth2 ->
                        oauth2.userInfoEndpoint(userInfo ->
                                userInfo.oidcUserService(
                                        oidcUserService
                                )
                        )
                )
                .build();
    }
}