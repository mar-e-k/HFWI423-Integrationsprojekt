package de.fhdw.vendix.commons.spring.vaadin.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.security.AuthenticationContext;

public abstract class AbstractApplicationLayout extends AppLayout {

    private final AuthenticationContext authenticationContext;

    public AbstractApplicationLayout(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;

        Component[] navbarItems = navbarItems();
        addToNavbar(navbarItems);

        Component[] drawerItems = draweritems();
        addToDrawer(drawerItems);
    }

    protected Component[] navbarItems() {
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

    protected abstract Component[] draweritems();

    protected abstract String applicationTitle();

    protected Button createDrawerToggle() {
        return new DrawerToggle();
    }

    protected Component createApplicationTitle() {
        return new H1(applicationTitle());
    }

    protected Button createNotificationButton() {
        return new Button(VaadinIcon.BELL.create());
    }

    protected Popover createNotificationPopover(Button notificationButton) {
        Popover notificationPopover = new Popover();
        notificationPopover.setTarget(notificationButton);
        notificationPopover.setWidth("20em");
        notificationPopover.setModal(true);

        return notificationPopover;
    }

    protected Button createAccountButton() {
        return new Button(VaadinIcon.USER.create());
    }

    protected Popover createAccountPopover(Button accountButton) {
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