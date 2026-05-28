package de.fhdw.vendix.commons.spring.vaadin.layout;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.security.AuthenticationContext;
import de.fhdw.vendix.commons.spring.vaadin.component.AccountComponent;
import de.fhdw.vendix.commons.spring.vaadin.component.NotificationComponent;

public abstract class AbstractApplicationLayout extends AppLayout {

    protected final AuthenticationContext authenticationContext;

    protected AbstractApplicationLayout(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    protected void initLayout() {
        addToNavbar(navbar());
        addToDrawer(drawer());
    }

    protected HorizontalLayout navbar() {
        HorizontalLayout left = new HorizontalLayout(
                new DrawerToggle(),
                new H1(applicationTitle())
        );

        Div spacer = new Div();

        HorizontalLayout right = new HorizontalLayout(
                new NotificationComponent(),
                new AccountComponent(authenticationContext)
        );

        HorizontalLayout navbar = new HorizontalLayout(left, spacer, right);
        navbar.setWidthFull();
        navbar.expand(spacer);

        return navbar;
    }

    protected abstract VerticalLayout drawer();

    protected abstract String applicationTitle();
}