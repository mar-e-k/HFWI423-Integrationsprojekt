package de.fhdw.vendix.store.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.aura.Aura;
import de.fhdw.vendix.commons.spring.security.context.store.StoreContext;
import de.fhdw.vendix.store.core.domain.store.service.StoreService;
import de.fhdw.vendix.store.ui.account.RoleView;
import de.fhdw.vendix.store.ui.account.UserView;
import de.fhdw.vendix.store.ui.account.PermissionView;
import de.fhdw.vendix.store.ui.business.*;
import de.fhdw.vendix.store.ui.home.ConnectionsView;
import de.fhdw.vendix.store.ui.home.DebugView;
import de.fhdw.vendix.store.ui.home.HomeView;
import de.fhdw.vendix.store.ui.home.StoreContextView;

import java.util.Objects;

@Layout
@StyleSheet(Aura.STYLESHEET)
@AnonymousAllowed
@VaadinSessionScope
public class StoreAppLayout extends AppLayout {

    private final AuthenticationContext authenticationContext;
    private final StoreContext storeContext;
    private final StoreService storeService;

    public StoreAppLayout(AuthenticationContext authenticationContext, StoreContext storeContext, StoreService storeService) {
        this.authenticationContext = authenticationContext;
        this.storeContext = storeContext;
        this.storeService = storeService;

        addToNavbar(
                createNavbarItems()
        );

        addToDrawer(
                createDrawerItems()
        );
    }

    private Component[] createNavbarItems() {
        Button notificationButton = createNotificationButton();
        Popover notificationPopover = createNotificationPopover(notificationButton);
        Button accountButton = createAccountButton();
        Popover accountPopover = createAccountPopover(accountButton);

        return new Component[]{
                createDrawerToggle(),
                createApplicationTitle(),
                notificationButton,
                notificationPopover,
                accountButton,
                accountPopover
        };
    }

    private Component[] createDrawerItems() {
        SideNav managementHeader = new SideNav();
        managementHeader.setLabel("Management");
        managementHeader.addItem(
                new SideNavItem("Users", UserView.class, VaadinIcon.USERS.create()),
                new SideNavItem("Roles", RoleView.class, VaadinIcon.USER_CARD.create()),
                new SideNavItem("Permissions", PermissionView.class, VaadinIcon.KEY.create())
        );

        SideNav businessHeader = new SideNav();
        businessHeader.setLabel("Business");
        businessHeader.addItem(
                new SideNavItem("Stores", StoreView.class, VaadinIcon.SHOP.create()),
                new SideNavItem("Registers", RegisterView.class, VaadinIcon.DESKTOP.create()),
                new SideNavItem("Stocks", StockView.class, VaadinIcon.STOCK.create()),
                new SideNavItem("Receipts", ReceiptView.class, VaadinIcon.NEWSPAPER.create())
        );

        SideNav externalHeader = new SideNav();
        externalHeader.setLabel("External");
        externalHeader.addItem(
                new SideNavItem("Grafana", "http://localhost:3030", VendixIcon.GRAFANA.create()),
                new SideNavItem("Neon", "https://console.neon.tech/app/projects/bitter-feather-66001186", VendixIcon.NEON_DB.create()),
                new SideNavItem("CloudAMQP", "https://api.cloudamqp.com/console/649e89f9-9795-4012-9e9b-fbf61637e747/details", VendixIcon.CLOUD_AMQP.create()),
                new SideNavItem("Swagger", "http://localhost:8080/swagger", VendixIcon.SWAGGER.create())
        );


        SideNavItem connectionsNavItem = new SideNavItem("Connections", ConnectionsView.class, VaadinIcon.CONNECT.create());
        connectionsNavItem.setSuffixComponent(
                createActiveRegistersBadge()
        );

        SideNavItem contextNavItem = new SideNavItem("Context", StoreContextView.class, VaadinIcon.FLASH.create());
        contextNavItem.setSuffixComponent(
                createStoreContextBadge()
        );

        return new Component[]{
                new SideNavItem("Home", HomeView.class, VaadinIcon.HOME.create()),
                connectionsNavItem,
                contextNavItem,
                new SideNavItem("Debug", DebugView.class, VaadinIcon.COGS.create()),
                managementHeader,
                businessHeader,
                externalHeader,
        };
    }

    private Button createDrawerToggle() {
        return new DrawerToggle();
    }

    private Component createApplicationTitle() {
        return new H1("Store");
    }

    private Badge createActiveRegistersBadge() {
        Badge activeRegistersBadge = new Badge();
        if (storeContext.getStore() == null) {
            activeRegistersBadge.setText("N/A");
            activeRegistersBadge.addThemeVariants(BadgeVariant.ERROR);
        } else {
            int registersCount = storeService.getAllRegisters(Objects.requireNonNull(storeContext.getStore().id())).size();
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

    private Button createNotificationButton() {
        return new Button(VaadinIcon.BELL.create());
    }

    private Popover createNotificationPopover(Button notificationButton) {
        Popover notificationPopover = new Popover();
        notificationPopover.setTarget(notificationButton);
        notificationPopover.setWidth("20em");
        notificationPopover.setModal(true);

        return notificationPopover;
    }

    private Button createAccountButton() {
        return new Button(VaadinIcon.USER.create());
    }

    private Popover createAccountPopover(Button accountButton) {
        Popover accountPopover = new Popover();
        accountPopover.setTarget(accountButton);
        accountPopover.setWidth("20em");
        accountPopover.setModal(true);

        VerticalLayout accountInformationLayout = new VerticalLayout();
        accountInformationLayout.setWidthFull();
        accountInformationLayout.setMargin(true);
        accountInformationLayout.setSpacing(true);
        TextField accountNameTextField = new TextField(
                "Account Name",
                authenticationContext.getPrincipalName()
                        .orElse("Nothing"),
                "Account Name"
        );
        accountNameTextField.setReadOnly(true);
        TextField accountRolesTextField = new TextField(
                "Account Roles",
                authenticationContext.getGrantedRoles().toString(),
                "Account Roles"
        );
        accountRolesTextField.setReadOnly(true);
        accountInformationLayout.add(accountNameTextField, accountRolesTextField);

        Button logoutButton = new Button("Logout", VaadinIcon.SIGN_OUT.create(), _ -> authenticationContext.logout());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        logoutButton.addClickListener(_ -> authenticationContext.logout());

        accountPopover.add(accountInformationLayout, logoutButton);

        return accountPopover;
    }
}