package de.fhdw.fillialensystem.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.fhdw.commons.utility.AuthContext;
import de.fhdw.commons.view.AbstractLoginView;
import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.service.AccountService;
import de.fhdw.fillialensystem.utility.security.JwtService;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Route("/login")
@PageTitle("Login View")
@PermitAll
@AnonymousAllowed
public class LoginView extends AbstractLoginView {

    private final JwtService jwtService;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    public LoginView(JwtService jwtService, AccountService accountService, PasswordEncoder passwordEncoder) {
        super();
        this.jwtService = jwtService;
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
        loginForm.addLoginListener(this::onLogin);
        add(loginForm);
    }

    private void onLogin(AbstractLogin.LoginEvent event) {
        String username = event.getUsername();
        String password = event.getPassword();

        try {
            Account account = accountService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown account"));

            if (!passwordEncoder.matches(password, account.getPassword())) {
                loginForm.setError(true);
                return;
            }

            AuthContext authContext = new AuthContext(
                    account.getAccountRole().getRole(),
                    account.getUuid(),
                    account.getUsername(),
                    null,
                    null
            );

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            authContext,
                            null,
                            authContext.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);

            String jwt = jwtService.generateToken(authContext);

            VaadinSession.getCurrent().setAttribute("jwt", jwt);
            VaadinSession.getCurrent().setAttribute("auth-context", authContext);

            UI.getCurrent().navigate(MainView.class);
        } catch (Exception e) {
            loginForm.setError(true);
        }
    }
}