package de.fhdw.kassensystem.utility.config;

import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import de.fhdw.kassensystem.view.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        http.with(VaadinSecurityConfigurer.vaadin(), configurer ->
                configurer.loginView(LoginView.class));

        http.logout(logout -> {
            logout.logoutRequestMatcher(request -> request.getMethod().equals(HttpMethod.GET.name()) && request.getRequestURI().equals("/logout"));
            logout.logoutSuccessUrl("/login?logout"); // Redirect to login page
        });

        return http.build();
    }

    // Hier sind die UserDetails mit Passwort und Username
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails cashier = User.withUsername("cashier")
                .password(passwordEncoder.encode("password"))
                .roles(Roles.Type.CASHIER)
                .build();

        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder.encode("password"))
                .roles(Roles.Type.ADMIN)
                .build();

        return new InMemoryUserDetailsManager(cashier, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
