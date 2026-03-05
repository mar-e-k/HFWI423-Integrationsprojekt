package de.fhdw.vendix.commons.spring.starter.autoconfigure;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.api.domain.lock.port.LockQueryPort;
import de.fhdw.vendix.commons.security.auth.AuthWhitelist;
import de.fhdw.vendix.commons.security.spring.DefaultAppContext;
import de.fhdw.vendix.commons.security.spring.AuthContextHolder;
import de.fhdw.vendix.commons.security.spring.DefaultUserDetailsService;
import de.fhdw.vendix.commons.security.spring.JwtAuthenticationFilter;
import de.fhdw.vendix.commons.security.spring.DefaultClassAccessChecker;
import de.fhdw.vendix.commons.spring.starter.properties.SecurityPropertiesConfiguration;
import de.fhdw.vendix.security.api.auth.AppContext;
import de.fhdw.vendix.security.api.auth.AuthContext;
import de.fhdw.vendix.security.api.ui.ClassAccessChecker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Optional;
import java.util.UUID;

@AutoConfiguration
@EnableConfigurationProperties(SecurityPropertiesConfiguration.class)
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditorAware<String> auditorAware() {
        return () -> AuthContextHolder.current()
                .map(AuthContext::account)
                .map(AccountDTO::username)
                .or(() -> Optional.of("unknown"));
    }

    @Bean
    @ConditionalOnMissingBean
    public ClassAccessChecker classAccessChecker() {
        return new DefaultClassAccessChecker();
    }

    @Bean
    @ConditionalOnMissingBean
    public AppContext appContext(Environment environment) {
        return new DefaultAppContext(environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter, SecurityPropertiesConfiguration securityPropertiesConfiguration) {
        if (!securityPropertiesConfiguration.enabled()) {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(request -> request.anyRequest().permitAll())
                    .build();
        } else {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                    .authorizeHttpRequests(request -> request
                            .requestMatchers(AuthWhitelist.API_WHITELIST.toArray(String[]::new)).permitAll()
                            .requestMatchers("/api/**").authenticated()
                            .anyRequest().authenticated())
                    .formLogin(form -> form
                            .loginPage("/login")
                            .permitAll())
                    .logout(logout -> logout
                                    .logoutUrl("/logout")
                                    .logoutSuccessUrl("/login?logout")
//                            .invalidateHttpSession(true)
//                            .clearAuthentication(true)
                    )
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .build();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultUserDetailsService customUserDetailsService(AccountQueryPort accountQueryPort, LockQueryPort lockQueryPort) {
        return new DefaultUserDetailsService(accountQueryPort, lockQueryPort);
    }
}