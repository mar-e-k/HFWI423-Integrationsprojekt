package de.fhdw.vendix.store.ui.account;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRoleDTO;
import de.fhdw.vendix.store.ui.StoreAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "role", layout = StoreAppLayout.class)
@RolesAllowed(Role.ROLE_ADMIN)
public class RoleView extends VerticalLayout {

    public RoleView() {
        add(initGrid());
    }

    private Grid<AccountRoleDTO> initGrid() {
        Grid<AccountRoleDTO> accountGrid = new Grid<>(AccountRoleDTO.class, false);
        accountGrid.setHeightFull();
        accountGrid.setWidthFull();
        accountGrid.addColumn(AccountRoleDTO::id)
                .setHeader("ID")
                .setAutoWidth(true)
                .setSortable(true);
        accountGrid.addColumn(AccountRoleDTO::role)
                .setHeader("Role")
                .setAutoWidth(true)
                .setSortable(true);
        return accountGrid;
    }
}