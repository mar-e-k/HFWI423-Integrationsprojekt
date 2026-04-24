package de.fhdw.vendix.store.ui;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import com.vaadin.flow.spring.security.AuthenticationContext;
import de.fhdw.vendix.commons.spring.security.Role;
import de.fhdw.vendix.commons.spring.vaadin.layout.AbstractApplicationLayout;
import de.fhdw.vendix.store.ui.business.ReceiptView;
import de.fhdw.vendix.store.ui.business.StockView;
import de.fhdw.vendix.store.ui.business.VoucherView;
import de.fhdw.vendix.store.ui.home.ConnectionsView;
import de.fhdw.vendix.store.ui.home.DebugView;
import de.fhdw.vendix.store.ui.home.RootView;
import de.fhdw.vendix.store.ui.home.StoreContextView;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed({Role.Constants.ADMIN})
@Layout
@VaadinSessionScope
public class StoreAppLayout extends AbstractApplicationLayout {

    public StoreAppLayout(AuthenticationContext authenticationContext) {
        super(authenticationContext);
        initLayout();
    }

    @Override
    protected VerticalLayout drawer() {
        SideNav applicationHeader = new SideNav("Application");
        applicationHeader.addItem(
                new SideNavItem("Home", RootView.class, VaadinIcon.HOME.create()),
                new SideNavItem("Connection", ConnectionsView.class, VaadinIcon.CONNECT.create()),
                new SideNavItem("Context", StoreContextView.class, VaadinIcon.FLASH.create()),
                new SideNavItem("Debug", DebugView.class, VaadinIcon.COGS.create())
        );

        SideNav storeHeader = new SideNav("Store");
        storeHeader.addItem(
                new SideNavItem("Stocks", StockView.class, VaadinIcon.STOCK.create()),
                new SideNavItem("Receipts", ReceiptView.class, VaadinIcon.INVOICE.create()),
                new SideNavItem("Vouchers", VoucherView.class, VaadinIcon.HEALTH_CARD.create())
        );

        return new VerticalLayout(
                applicationHeader,
                storeHeader
        );
    }

    @Override
    protected String applicationTitle() {
        return "Store";
    }
}