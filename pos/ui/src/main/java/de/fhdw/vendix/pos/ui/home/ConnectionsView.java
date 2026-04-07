package de.fhdw.vendix.pos.ui.home;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.pos.ui.PosAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "connection", layout = PosAppLayout.class)
@RolesAllowed(Role.ROLE_CASHIER)
public class ConnectionsView extends VerticalLayout {

    public ConnectionsView() {}
}