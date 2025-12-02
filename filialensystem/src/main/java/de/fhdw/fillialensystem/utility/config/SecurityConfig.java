package de.fhdw.fillialensystem.utility.config;

import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import de.fhdw.fillialensystem.persistence.repository.AccountRepository;
import de.fhdw.fillialensystem.utility.StoreClient;
import de.fhdw.fillialensystem.utility.security.CustomUserDetailsService;
import de.fhdw.fillialensystem.utility.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
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
                            .anyRequest().authenticated())
                    .formLogin(form -> form
                            .loginPage("/login")
                            .permitAll()
                    )
                    .logout(logout -> logout
                            .logoutUrl("/logout")
                            .logoutSuccessUrl("/login?logout"))
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .build();
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(AccountRepository accountRepository, StoreClient storeClient) {
        return new CustomUserDetailsService(accountRepository, storeClient);
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        // This manual configuration is necessary to break a circular dependency.
        // The default AuthenticationManager from AuthenticationConfiguration can cause a StackOverflowError
        // if the UserDetailsService depends on other beans that are secured with method-level security,
        // as it creates a loop: AuthenticationManager -> UserDetailsService -> Secured Bean -> AuthenticationManager.
        // By creating a simple ProviderManager directly, we create a "clean" manager for the login process
        // that is not wrapped with the method security interceptors.
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}