package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.fhdw.kassensystem.utility.config.Roles;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route("")
@PermitAll
public class MainView extends VerticalLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) { //Sollte richtige MainView einbauen
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Prüfe, ob der Benutzer authentifiziert und kein anonymer Benutzer ist
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            // Benutzer ist eingeloggt, prüfe die Rolle
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(r -> r.getAuthority().equals(Roles.Type.ADMIN));

            if (isAdmin) {
                event.rerouteTo(AdminView.class);
            } else {
                event.rerouteTo(CashierView.class);
            }
        } else {
            // Benutzer ist nicht eingeloggt, leite direkt zum Login weiter
            event.rerouteTo(LoginView.class);
        }
    }
}
