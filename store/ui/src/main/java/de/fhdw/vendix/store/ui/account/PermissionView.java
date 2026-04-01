package de.fhdw.vendix.store.ui.account;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRole;
import de.fhdw.vendix.store.ui.StoreAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "permissions", layout = StoreAppLayout.class)
@RolesAllowed(AccountRole.ROLE_ADMIN)
public class PermissionView extends VerticalLayout {

    public PermissionView() {}
}