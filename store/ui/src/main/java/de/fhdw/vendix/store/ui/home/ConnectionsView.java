package de.fhdw.vendix.store.ui.home;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.store.ui.StoreAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "connection", layout = StoreAppLayout.class)
@RolesAllowed(Role.ROLE_ADMIN)
public class ConnectionsView extends VerticalLayout {

    public ConnectionsView() {}
}