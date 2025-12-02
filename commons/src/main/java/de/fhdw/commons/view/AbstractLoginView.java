package de.fhdw.commons.view;

import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class AbstractLoginView extends VerticalLayout {

    protected final LoginForm loginForm = new LoginForm();
    protected final NativeLabel warningLabel = new NativeLabel();

    public AbstractLoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(true);
        loginForm.addForgotPasswordListener(event ->
                Notification.show(
                        "Bitte wende dich an einen Administrator, um dein Passwort zurückzusetzen",
                        3000,
                        Notification.Position.MIDDLE));

        add(loginForm, warningLabel);
    }
}