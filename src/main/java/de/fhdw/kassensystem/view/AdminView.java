package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route("admin")
@RolesAllowed("ADMIN")
public class AdminView extends VerticalLayout {

    public AdminView() {
        H1 title = new H1("Admin-Dashboard");

        Button logoutButton = new Button("Logout", e -> {
            // Spring Security handles the logout by default at this URL
            UI.getCurrent().getPage().setLocation("/logout");
        });

        setAlignItems(Alignment.CENTER);
        add(title, logoutButton);
    }
}