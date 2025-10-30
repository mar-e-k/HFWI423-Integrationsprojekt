package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("/login")
@PermitAll // Wichtig: Erlaubt jedem, die Login-Seite zu sehen
@PageTitle("Login View")
public class LoginView extends VerticalLayout {

    public LoginView() {
        H1 title = new H1("Kassensystem für Integrationsprojekt");

        LoginForm loginForm = new LoginForm();
        loginForm.setAction("login");

        // Event-Listener für den "Passwort vergessen"-Button hinzufügen
        loginForm.addForgotPasswordListener(event -> {
            Notification.show("Pech gehabt! Spaß kontaktiere Erik oder Rohid um das Passwort zu erhalten",
                              5000, // Dauer in Millisekunden
                              Notification.Position.MIDDLE);
        });

        add(title, loginForm);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();
    }
}