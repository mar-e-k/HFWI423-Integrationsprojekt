package de.fhdw.vendix.commons.ui.vaadin.view;

import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import de.fhdw.vendix.commons.ui.vaadin.components.ScrollingTextBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public abstract class AbstractLoginView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger log = LoggerFactory.getLogger(AbstractLoginView.class);

    private final AuthenticationManager authenticationManager;

    private final LoginForm loginForm;
    private final ScrollingTextBar scrollingTextBar;

    protected AbstractLoginView(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        this.loginForm = new LoginForm();
        this.scrollingTextBar = new ScrollingTextBar("DANGER");
        add(scrollingTextBar);
        configureLoginForm();
    }

    private void configureLoginForm() {
        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.addLoginListener(this::onLoginEvent);

        super.setAlignItems(Alignment.CENTER);
        super.setJustifyContentMode(JustifyContentMode.CENTER);
        super.setSizeFull();
        super.add(loginForm);
    }

    private void onLoginEvent(AbstractLogin.LoginEvent event) {
        Authentication token = new UsernamePasswordAuthenticationToken(event.getUsername(), event.getPassword());
        try {
            Authentication authentication = authenticationManager.authenticate(token);
            log.atInfo().log("Success");
        } catch (AuthenticationException e) {
            loginForm.showErrorMessage("Test", "Test");
            log.atError().log(e.getMessage());
        }
        log.atInfo().log("Username: '{}'", event.getUsername());
        log.atInfo().log("Password: '{}'", event.getPassword());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {

    }
}