package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.fhdw.kassensystem.utility.CustomUserDetails;

@Route("/login")
@PageTitle("Login View")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView(CustomUserDetails customUserDetails) {

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("Kassensystem für Integrationsprojekt");

        login.setAction("login");
        login.addForgotPasswordListener(event -> Notification.show("Pech gehabt! Spaß kontaktiere Erik oder Rohid um das Passwort zu erhalten",
                5000, // Dauer in Millisekunden
                Notification.Position.MIDDLE));

        add(title, login);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            login.setError(true);
        }
    }
}