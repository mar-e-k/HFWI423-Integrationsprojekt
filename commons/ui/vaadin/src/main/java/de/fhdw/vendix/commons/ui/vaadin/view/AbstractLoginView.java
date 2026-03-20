package de.fhdw.vendix.commons.ui.vaadin.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.spring.security.AuthenticationContext;
import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.port.AccountCommandPort;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleDTO;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.api.domain.account_role.port.AccountRoleCommandPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractLoginView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger log = LoggerFactory.getLogger(AbstractLoginView.class);

    private final AuthenticationContext context;
    private final PasswordEncoder passwordEncoder;

    private final AccountCommandPort accountCommandPort;
    private final AccountRoleCommandPort accountRoleCommandPort;

    private final LoginForm loginForm;

    protected AbstractLoginView(
            AuthenticationContext context,
            PasswordEncoder passwordEncoder,
            AccountCommandPort accountCommandPort, AccountRoleCommandPort accountRoleCommandPort
    ) {
        this.context = context;
        this.passwordEncoder = passwordEncoder;
        this.accountCommandPort = accountCommandPort;
        this.accountRoleCommandPort = accountRoleCommandPort;
        this.loginForm = new LoginForm();
        configureLoginForm();
        configureCreateDummyAdminButton();
        configureAddRoleButton();
        configureAddAssignmentButton();
        configureCreateLogoutButton();
    }

    private void configureLoginForm() {
        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.setAction("login");

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
            accountCommandPort.create(admin);
        });
        add(createDummyAdminButton);
    }

    private void configureAddRoleButton() {
        Button addRoleButton = new Button("Add roles");
        addRoleButton.addClickListener(e -> {
            for (AccountRoleEnum role : AccountRoleEnum.values()) {
                accountRoleCommandPort.create(new AccountRoleDTO(null, role));
            }
        });
        add(addRoleButton);
    }

    private void configureAddAssignmentButton() {
        Button  addAssignmentButton = new Button("Add assignment");
        addAssignmentButton.addClickListener(e -> {
            accountCommandPort.assignRole(1L, 1L);
            accountCommandPort.assignRole(1L, 2L);
            accountCommandPort.assignRole(1L, 3L);
        });
        add(addAssignmentButton);
    }

    private void configureCreateLogoutButton() {
        Button logout = new Button("Logout");
        logout.addClickListener(e -> {
            context.logout();
        });
        add(logout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        log.atInfo().log(SecurityContextHolder.getContext().toString());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            log.atInfo().log(authentication.toString());
            log.atInfo().log(authentication.getPrincipal().toString());
        }
    }
}