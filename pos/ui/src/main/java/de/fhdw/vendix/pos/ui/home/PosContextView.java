package de.fhdw.vendix.pos.ui.home;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.aura.Aura;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.pos.ui.PosAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "context", layout = PosAppLayout.class)
@RolesAllowed(Role.ROLE_CASHIER)
@StyleSheet(Aura.STYLESHEET)
public class PosContextView extends VerticalLayout {

    public PosContextView() {}
}