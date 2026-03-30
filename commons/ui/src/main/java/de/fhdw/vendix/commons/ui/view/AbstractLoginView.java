package de.fhdw.vendix.commons.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.theme.aura.Aura;
import de.fhdw.vendix.commons.security.auth.AuthContext;
import de.fhdw.vendix.commons.security.auth.AuthContextHolder;

import java.util.Optional;

@StyleSheet(Aura.STYLESHEET)
public abstract class AbstractLoginView extends VerticalLayout implements BeforeEnterObserver {

    public AbstractLoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        Button button = new Button("Debug Exception");
        button.addClickListener(event -> {
            throw new IllegalArgumentException("Debug Exception");
        });
        add(button);

        LoginForm loginForm = new LoginForm();
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(true);
        loginForm.addForgotPasswordListener(event -> Notification.show(
                "Bitte wende dich an einen Administrator, um dein Passwort zurückzusetzen",
                3000,
                Notification.Position.MIDDLE));
        add(loginForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Optional<AuthContext> authContext = AuthContextHolder.current();

        if (authContext.isPresent()) {
            AuthContextHolder.clear();
        }
    }
}