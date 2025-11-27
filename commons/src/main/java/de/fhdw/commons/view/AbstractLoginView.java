package de.fhdw.commons.view;


import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.util.List;

public abstract class AbstractLoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();
    private final NativeLabel warningLabel = new NativeLabel();

    public AbstractLoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        init();
        add(loginForm, warningLabel);
    }

    private void init() {
        // ---- LoginForm ----
        loginForm.setAction("login");
        loginForm.addForgotPasswordListener(event ->
                Notification.show(
                        "Bitte wende dich an einem Administrator in deiner Filliale, um dein Password zurückzusetzen zu lassen",
                        3000,
                        Notification.Position.MIDDLE));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (!existsAtLeastOneAdminAccount()) {
            warningLabel.setText("Kein Admin Konto vorhanden");
        } else if (!existsAtLeastOneCashierAccount()) {
            warningLabel.setText("Kein Cashier Konto vorhanden");
        } else {
            warningLabel.setVisible(false);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        String errorParam = beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault("error", List.of())
                .stream()
                .findFirst()
                .orElse(null);

        if (errorParam == null || errorParam.isEmpty()) {
            return;
        }

        switch (ErrorQueryParameter.valueOf(errorParam.toUpperCase().replaceAll("-", "_"))) {
            case ErrorQueryParameter.LOGIN_REQUIRED:
                Notification.show(
                        "Account wird für die Operation benötigt. Bitte anmelden",
                        5000,
                        Notification.Position.TOP_CENTER);
                break;
            case ErrorQueryParameter.ROLES_MISSING:
                Notification.show(
                        "Account hat keine Berechtigungen. Bitte kontaktiere die IT",
                        5000,
                        Notification.Position.TOP_CENTER);
                break;
            case ErrorQueryParameter.ACCESS_DENIED:
                Notification.show(
                        "Account hat nicht die benötigten Berechtigungen",
                        5000,
                        Notification.Position.TOP_CENTER);
                break;
            default:
                Notification.show(
                        "Unbekannter Fehler. Bitte kontaktiere die IT",
                        5000,
                        Notification.Position.TOP_CENTER);
        }
    }

    protected abstract boolean existsAtLeastOneAdminAccount();

    protected abstract boolean existsAtLeastOneCashierAccount();
}