package de.fhdw.fillialensystem.utility.config;

import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import de.fhdw.fillialensystem.utility.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.RememberMeConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
public class SecurityConfig {

    private final boolean springSecurityEnabled;

    private static final String[] WHITELIST = {
            "/",
            "/login",
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, HttpFirewall httpFirewall, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http.setSharedObject(HttpFirewall.class, httpFirewall);
        if (!springSecurityEnabled) {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        } else {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(WHITELIST).permitAll()
                            .requestMatchers("/api/**").authenticated()
                            .anyRequest().fullyAuthenticated())
                    .formLogin(form -> form
                            .loginPage("/login")
                            .permitAll())
                    .logout(logout -> logout
                            .logoutUrl("/logout")
                            .logoutSuccessUrl("/login?logout"))
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .rememberMe(RememberMeConfigurer::disable)
                    .build();
        }
    }

    @Bean
    public HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        firewall.setAllowUrlEncodedPercent(true);
        firewall.setAllowSemicolon(true);
        firewall.setAllowBackSlash(true);
        firewall.setAllowUrlEncodedPeriod(true);
        return firewall;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(HttpFirewall firewall) {
        return (web) -> web.httpFirewall(firewall);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}