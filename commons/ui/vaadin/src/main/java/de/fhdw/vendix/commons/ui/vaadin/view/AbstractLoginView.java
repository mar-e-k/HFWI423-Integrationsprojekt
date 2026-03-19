package de.fhdw.vendix.commons.ui.vaadin.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.port.AccountCommandPort;
import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.api.structure.port.CrudCommandPort;
import de.fhdw.vendix.commons.api.structure.port.CrudQueryPort;
import de.fhdw.vendix.commons.security.spring.authentication.AuthenticationHandler;
import de.fhdw.vendix.commons.security.spring.token.AuthenticationRequestToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractLoginView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger log = LoggerFactory.getLogger(AbstractLoginView.class);

    private final AuthenticationHandler authenticationHandler;
    private final PasswordEncoder passwordEncoder;

    private final AccountQueryPort accountQueryPort;
    private final AccountCommandPort accountCommandPort;

    private final CrudQueryPort<AccountDTO, Long> crudQueryPort;
    private final CrudCommandPort<AccountDTO, Long> crudCommandPort;

    private final LoginForm loginForm;

    protected AbstractLoginView(
            AuthenticationHandler authenticationHandler,
            PasswordEncoder passwordEncoder,
            AccountQueryPort accountQueryPort,
            AccountCommandPort accountCommandPort,
            CrudQueryPort<AccountDTO, Long> crudQueryPort,
            CrudCommandPort<AccountDTO, Long> crudCommandPort
    ) {
        this.authenticationHandler = authenticationHandler;
        this.passwordEncoder = passwordEncoder;
        this.accountQueryPort = accountQueryPort;
        this.accountCommandPort = accountCommandPort;
        this.crudQueryPort = crudQueryPort;
        this.crudCommandPort = crudCommandPort;
        this.loginForm = new LoginForm();
        configureLoginForm();
        configureCreateDummyAdminButton();
        configureCreateLogoutButton();
    }

    private void configureLoginForm() {
        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.addLoginListener(this::onLoginEvent);

        super.setAlignItems(Alignment.CENTER);
        super.setJustifyContentMode(JustifyContentMode.CENTER);
        super.setSizeFull();
        super.add(loginForm);
    }

    private void configureCreateDummyAdminButton() {
        Button createDummyAdminButton = new Button("Create dummy admin");
        createDummyAdminButton.addClickListener(e -> {
            String password = passwordEncoder.encode("system");
            Objects.requireNonNull(password);
            AccountDTO admin = new AccountDTO(
                    null,
                    UUID.randomUUID(),
                    "system",
                    password
            );
            crudCommandPort.create(admin);
        });
        add(createDummyAdminButton);
    }

    private void configureCreateLogoutButton() {
        Button logout = new Button("Logout");
        logout.addClickListener(e -> {
            authenticationHandler.logout(
                    VaadinServletRequest.getCurrent().getHttpServletRequest(),
                    VaadinServletResponse.getCurrent().getHttpServletResponse()
            );
        });
        add(logout);
    }

    private void onLoginEvent(AbstractLogin.LoginEvent event) {
        Authentication token = new AuthenticationRequestToken(event.getUsername(), event.getPassword());
        try {
            authenticationHandler.login(
                    token,
                    VaadinServletRequest.getCurrent().getHttpServletRequest(),
                    VaadinServletResponse.getCurrent().getHttpServletResponse()
            );
            log.atInfo().log("Success");
        } catch (AuthenticationException e) {
            loginForm.showErrorMessage(e.getClass().getSimpleName(), e.getMessage());
            log.atError().log(e.getMessage());
        }
        log.atInfo().log("Username: '{}'", event.getUsername());
        log.atInfo().log("Password: '{}'", event.getPassword());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        log.atInfo().log(SecurityContextHolder.getContext().toString());
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.atInfo().log(SecurityContextHolder.getContext().getAuthentication().toString());
        }
        log.atInfo().log("count={}", crudQueryPort.count());
        log.atInfo().log("admin={}", accountQueryPort.existsByRole(AccountRoleEnum.ADMIN));
        log.atInfo().log("system={}", accountQueryPort.existsByRole(AccountRoleEnum.SYSTEM));
        log.atInfo().log("cashier={}", accountQueryPort.existsByRole(AccountRoleEnum.CASHIER));
    }
}