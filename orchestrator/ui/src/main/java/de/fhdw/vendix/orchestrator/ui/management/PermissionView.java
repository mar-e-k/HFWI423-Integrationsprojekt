package de.fhdw.vendix.orchestrator.ui.management;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.orchestrator.core.domain.account_role_assignment.AccountRoleAssignment;
import de.fhdw.vendix.orchestrator.core.domain.account_role_assignment.AccountRoleAssignmentService;
import de.fhdw.vendix.orchestrator.ui.OrchestratorAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "permissions", layout = OrchestratorAppLayout.class)
@RolesAllowed(Role.ROLE_ADMIN)
public class PermissionView extends VerticalLayout {

    private final AccountRoleAssignmentService accountRoleAssignmentService;

    public PermissionView(AccountRoleAssignmentService accountRoleAssignmentService) {
        this.accountRoleAssignmentService = accountRoleAssignmentService;
        add(initGrid());
    }

    private Grid<AccountRoleAssignment> initGrid() {
        Grid<AccountRoleAssignment> accountRoleAssignmentGrid = new Grid<>(AccountRoleAssignment.class, false);
        accountRoleAssignmentGrid.setHeightFull();
        accountRoleAssignmentGrid.setWidthFull();
        accountRoleAssignmentGrid.addColumn(AccountRoleAssignment::getId)
                .setHeader("ID")
                .setAutoWidth(true)
                .setSortable(true);
        accountRoleAssignmentGrid.addColumn(AccountRoleAssignment::getAccountId)
                .setHeader("Account")
                .setAutoWidth(true)
                .setSortable(true);
        accountRoleAssignmentGrid.addColumn(AccountRoleAssignment::getRoleId)
                .setHeader("Role")
                .setAutoWidth(true)
                .setSortable(true);
        accountRoleAssignmentGrid.setItems(accountRoleAssignmentService.findAll());
        return accountRoleAssignmentGrid;
    }
}