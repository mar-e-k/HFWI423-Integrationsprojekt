package de.fhdw.vendix.commons.spring.vaadin.component;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.security.AuthenticationContext;

public class AccountComponent extends Composite<Div> {

    private final AuthenticationContext authenticationContext;

    private final Button button = new Button(VaadinIcon.USERS.create());
    private final Popover popover = new  Popover();

    public AccountComponent(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;

        configureButton();
        configurePopover();

        getContent().add(button);
    }

    private void configureButton() {}

    private void configurePopover() {
        popover.setTarget(button);
        popover.setWidth("20em");
        popover.setModal(true);

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.setMargin(true);
        layout.setSpacing(true);

        TextField name = new TextField(
                "Account Name",
                authenticationContext.getPrincipalName().orElse("Nothing"),
                "Account Name"
        );
        name.setReadOnly(true);

        TextField roles = new TextField(
                "Account Roles",
                authenticationContext.getGrantedRoles().toString(),
                "Account Roles"
        );
        roles.setReadOnly(true);

        Button logout = new Button(
                "Logout",
                VaadinIcon.SIGN_OUT.create(),
                e -> authenticationContext.logout()
        );
        logout.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(name, roles);

        popover.add(layout, logout);
    }
}