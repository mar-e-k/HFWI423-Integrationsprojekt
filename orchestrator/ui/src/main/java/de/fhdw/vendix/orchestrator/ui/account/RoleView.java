package de.fhdw.vendix.orchestrator.ui.account;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.orchestrator.core.domain.account_role.AccountRole;
import de.fhdw.vendix.orchestrator.core.domain.account_role.AccountRoleService;
import de.fhdw.vendix.orchestrator.ui.OrchestratorAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "role", layout = OrchestratorAppLayout.class)
@RolesAllowed(Role.ROLE_ADMIN)
public class RoleView extends VerticalLayout {

    private final AccountRoleService accountRoleService;

    public RoleView(AccountRoleService accountRoleService) {
        this.accountRoleService = accountRoleService;
        add(initGrid());
    }

    private Grid<AccountRole> initGrid() {
        Grid<AccountRole> accountRoleGrid = new Grid<>(AccountRole.class, false);
        accountRoleGrid.setHeightFull();
        accountRoleGrid.setWidthFull();
        accountRoleGrid.addColumn(AccountRole::getId)
                .setHeader("ID")
                .setAutoWidth(true)
                .setSortable(true);
        accountRoleGrid.addColumn(AccountRole::getRole)
                .setHeader("Role")
                .setAutoWidth(true)
                .setSortable(true);
        accountRoleGrid.setItems(accountRoleService.findAll());
        return accountRoleGrid;
    }
}