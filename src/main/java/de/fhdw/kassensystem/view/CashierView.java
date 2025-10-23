package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;

@Route("cashier")
@RolesAllowed("CASHIER")
public class CashierView extends VerticalLayout {

    public CashierView() {
        H1 title = new H1("Kassenansicht");

        Button logoutButton = new Button("Logout", e -> {
            // Spring Security handles the logout by default at this URL
            UI.getCurrent().getPage().setLocation("/logout");
        });

        setAlignItems(Alignment.CENTER);
        add(title, logoutButton);
    }

}