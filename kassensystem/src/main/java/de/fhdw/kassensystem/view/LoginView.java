package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.fhdw.commons.api.dto.AccountDTO;
import de.fhdw.commons.utility.AuthContext;
import de.fhdw.commons.view.AbstractLoginView;
import de.fhdw.kassensystem.persistance.service.proxy.AccountProxyService;
import de.fhdw.kassensystem.utility.RegisterClient;
import de.fhdw.kassensystem.utility.StoreClient;
import de.fhdw.kassensystem.utility.security.JwtService;
import de.fhdw.kassensystem.view.cashier.CashierView;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Route("/login")
@PageTitle("Login View")
@PermitAll
@AnonymousAllowed
public class LoginView extends AbstractLoginView {

    private final JwtService jwtService;
    private final AccountProxyService accountProxyService;
    private final PasswordEncoder passwordEncoder;

    private final RegisterClient registerClient;
    private final StoreClient storeClient;

    public LoginView(JwtService jwtService, AccountProxyService accountProxyService, PasswordEncoder passwordEncoder, RegisterClient registerClient, StoreClient storeClient) {
        super();
        this.jwtService = jwtService;
        this.accountProxyService = accountProxyService;
        this.passwordEncoder = passwordEncoder;
        this.registerClient = registerClient;
        this.storeClient = storeClient;

        loginForm.addLoginListener(this::onLogin);
        add(loginForm);
    }

    private void onLogin(AbstractLogin.LoginEvent event) {
        String username = event.getUsername();
        String password = event.getPassword();

        try {
            AccountDTO account = accountProxyService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown account"));

            if (!passwordEncoder.matches(password, account.getPassword())) {
                loginForm.setError(true);
                return;
            }

            AuthContext authContext = new AuthContext(
                    account.getRole(),
                    account.getUuid(),
                    account.getUsername(),
                    storeClient.getStoreDTO().getId().intValue(),
                    registerClient.getRegisterDTO().getId().intValue()
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

            UI.getCurrent().navigate(CashierView.class);
        } catch (Exception e) {
            loginForm.setError(true);
        }
    }
}