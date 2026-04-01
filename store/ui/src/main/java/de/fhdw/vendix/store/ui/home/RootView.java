package de.fhdw.vendix.store.ui.home;

import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRole;
import de.fhdw.vendix.commons.ui.vaadin.view.AbstractRootView;
import de.fhdw.vendix.store.ui.StoreAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "", layout = StoreAppLayout.class)
@RolesAllowed(AccountRole.ROLE_ADMIN)
public class RootView extends AbstractRootView {

    public RootView() {}
}