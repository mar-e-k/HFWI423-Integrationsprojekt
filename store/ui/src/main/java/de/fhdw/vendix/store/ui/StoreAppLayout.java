package de.fhdw.vendix.store.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import com.vaadin.flow.spring.security.AuthenticationContext;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.spring.vaadin.view.AbstractApplicationLayout;
import de.fhdw.vendix.commons.spring.security.context.store.StoreContext;
import de.fhdw.vendix.store.ui.business.*;
import de.fhdw.vendix.store.ui.home.ConnectionsView;
import de.fhdw.vendix.store.ui.home.DebugView;
import de.fhdw.vendix.store.ui.home.RootView;
import de.fhdw.vendix.store.ui.home.StoreContextView;
import jakarta.annotation.security.RolesAllowed;

import java.util.Objects;

@RolesAllowed({Role.ROLE_ADMIN})
@Layout
@VaadinSessionScope
public class StoreAppLayout extends AbstractApplicationLayout {

    private final StoreContext storeContext;

    public StoreAppLayout(AuthenticationContext authenticationContext, StoreContext storeContext) {
        this.storeContext = storeContext;
        super(authenticationContext);
    }

    @Override
    protected Component[] draweritems() {
        SideNav applicationHeader = new SideNav("Application");
        SideNavItem connectionsNavItem = new SideNavItem("Connections", ConnectionsView.class, VaadinIcon.CONNECT.create());
        connectionsNavItem.setSuffixComponent(
                createActiveRegistersBadge()
        );
        SideNavItem contextNavItem = new SideNavItem("Context", StoreContextView.class, VaadinIcon.FLASH.create());
        contextNavItem.setSuffixComponent(
                createStoreContextBadge()
        );

        applicationHeader.addItem(
                new SideNavItem("Home", RootView.class, VaadinIcon.HOME.create()),
                connectionsNavItem,
                contextNavItem,
                new SideNavItem("Debug", DebugView.class, VaadinIcon.COGS.create())
        );

        SideNav businessHeader = new SideNav();
        businessHeader.setLabel("Business");
        businessHeader.addItem(
                new SideNavItem("Stocks", StockView.class, VaadinIcon.STOCK.create()),
                new SideNavItem("Receipts", ReceiptView.class, VaadinIcon.NEWSPAPER.create())
        );

        return new Component[]{
                new SideNavItem("Home", RootView.class, VaadinIcon.HOME.create()),
                connectionsNavItem,
                contextNavItem,
                new SideNavItem("Debug", DebugView.class, VaadinIcon.COGS.create()),
                businessHeader
        };
    }

    @Override
    protected String applicationTitle() {
        return "Store";
    }

    private Badge createActiveRegistersBadge() {
        Badge activeRegistersBadge = new Badge();
        if (storeContext.getStore() == null) {
            activeRegistersBadge.setText("N/A");
            activeRegistersBadge.addThemeVariants(BadgeVariant.ERROR);
        } else {
//            int registersCount = storeService.findAllRegisters(Objects.requireNonNull(storeContext.getStore().id())).size();
            int registersCount = 0; // TODO
            activeRegistersBadge.setText("%d / %d".formatted(0, registersCount));
            activeRegistersBadge.addThemeVariants(BadgeVariant.WARNING);
        }
        return activeRegistersBadge;
    }

    private Badge createStoreContextBadge() {
        Badge storeContextBadge = new Badge();
        if (storeContext.getStore() == null) {
            storeContextBadge.setIcon(VaadinIcon.EXCLAMATION_CIRCLE.create());
            storeContextBadge.setThemeVariants(BadgeVariant.WARNING);
        } else {
            storeContextBadge.setText("ID: %d".formatted(Objects.requireNonNull(storeContext.getStore().id(), "Store has null ID set")));
            storeContextBadge.setThemeVariants(BadgeVariant.SUCCESS);
        }
        return storeContextBadge;
    }
}