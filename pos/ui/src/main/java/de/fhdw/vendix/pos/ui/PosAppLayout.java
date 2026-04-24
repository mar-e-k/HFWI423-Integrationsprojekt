package de.fhdw.vendix.pos.ui;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import com.vaadin.flow.spring.security.AuthenticationContext;
import de.fhdw.vendix.commons.spring.security.Role;
import de.fhdw.vendix.commons.spring.vaadin.layout.AbstractApplicationLayout;
import de.fhdw.vendix.pos.ui.home.ConnectionsView;
import de.fhdw.vendix.pos.ui.home.DebugView;
import de.fhdw.vendix.pos.ui.home.PosContextView;
import de.fhdw.vendix.pos.ui.home.RootView;
import de.fhdw.vendix.pos.ui.register.RegisterView;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed({Role.Constants.ADMIN})
@Layout
@VaadinSessionScope
public class PosAppLayout extends AbstractApplicationLayout {

    public PosAppLayout(AuthenticationContext authenticationContext) {
        super(authenticationContext);
        initLayout();
    }

    @Override
    protected VerticalLayout drawer() {
        SideNav applicationHeader = new SideNav("Application");
        applicationHeader.addItem(
                new SideNavItem("Home", RootView.class, VaadinIcon.HOME.create()),
                new SideNavItem("Connection", ConnectionsView.class, VaadinIcon.CONNECT.create()),
                new SideNavItem("Context", PosContextView.class, VaadinIcon.FLASH.create()),
                new SideNavItem("Debug", DebugView.class, VaadinIcon.COGS.create())
        );

        SideNav registerHeader = new SideNav("Register");
        registerHeader.addItem(
            new SideNavItem("Register", RegisterView.class, VaadinIcon.MONEY_EXCHANGE.create())
        );

        return new VerticalLayout(
                applicationHeader,
                registerHeader
        );
    }

    @Override
    protected String applicationTitle() {
        return "POS";
    }
}