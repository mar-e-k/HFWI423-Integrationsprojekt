package de.fhdw.vendix.store.ui.home;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.spring.security.keycloak.KeycloakRole;
import de.fhdw.vendix.store.ui.StoreAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "connection", layout = StoreAppLayout.class)
@RolesAllowed(KeycloakRole.Constants.ADMIN)
public class ConnectionsView extends VerticalLayout {

    public ConnectionsView() {}
}