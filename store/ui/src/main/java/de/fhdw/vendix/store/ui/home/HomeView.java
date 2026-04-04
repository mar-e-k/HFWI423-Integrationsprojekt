package de.fhdw.vendix.store.ui.home;

import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.ui.vaadin.view.AbstractHomeView;
import de.fhdw.vendix.store.ui.StoreAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "home", layout =  StoreAppLayout.class)
@RolesAllowed(Role.ROLE_ADMIN)
public class HomeView extends AbstractHomeView {

    public HomeView() {}
}