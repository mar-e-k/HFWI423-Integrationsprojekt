package de.fhdw.vendix.commons.ui.vaadin.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AbstractLoginView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger log = LoggerFactory.getLogger(AbstractLoginView.class);

    private final AuthenticationContext context;

    private final LoginForm loginForm;

    protected AbstractLoginView(AuthenticationContext context) {
        this.context = context;
        this.loginForm = new LoginForm();
        configureLoginForm();
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