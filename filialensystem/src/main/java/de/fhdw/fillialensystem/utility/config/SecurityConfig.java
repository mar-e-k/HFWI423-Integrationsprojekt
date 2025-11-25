package de.fhdw.fillialensystem.utility.config;

import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import de.fhdw.fillialensystem.view.LoginView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
public class SecurityConfig {

    private final boolean springSecurityEnabled;

    private static final String[] WHITELIST = {
            "/",
            "/favicon.ico",
            "/robots.txt",
            "/manifest.webmanifest",
            "/sw.js",
            "/offline-page.html",
            "/icons/**",
            "/images/**",
            "/frontend/**",
            "/webjars/**",
            "/VAADIN/**",
            "/vaadinServlet/**",
            "/connect/**",
            "/UIDL/**",
            "/HEARTBEAT/**",
    };

    public SecurityConfig(@Value("${spring.security.enabled:true}") boolean springSecurityEnabled) {
        this.springSecurityEnabled = springSecurityEnabled;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (!springSecurityEnabled) {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        } else {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(WHITELIST).permitAll()
                            .requestMatchers("/view/**").permitAll()
                            .requestMatchers("/api/**").authenticated()
                            .anyRequest().permitAll())
                    .with(VaadinSecurityConfigurer.vaadin(), configurer ->
                            configurer.loginView(LoginView.class))
                    .logout(logout -> {
                        logout.logoutRequestMatcher(request ->
                                request.getMethod().equals(HttpMethod.GET.name()) &&
                                        request.getRequestURI().equals("/logout"));
                        logout.logoutSuccessUrl("/login?logout");
                    })
                    .build();
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}