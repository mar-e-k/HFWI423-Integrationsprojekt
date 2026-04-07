package de.fhdw.vendix.pos.ui.home;

import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.spring.vaadin.view.AbstractRootView;
import de.fhdw.vendix.pos.ui.PosAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "", layout = PosAppLayout.class)
@RolesAllowed(Role.ROLE_CASHIER)
public class RootView extends AbstractRootView {

    public RootView() {}
}