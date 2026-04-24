package de.fhdw.vendix.pos.ui.home;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.spring.security.Role;
import de.fhdw.vendix.pos.ui.PosAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "debug", layout = PosAppLayout.class)
@RolesAllowed(Role.Constants.CASHIER)
public class DebugView extends VerticalLayout {

    public DebugView() {}
}