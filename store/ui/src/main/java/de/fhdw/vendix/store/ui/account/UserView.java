package de.fhdw.vendix.store.ui.account;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.store.ui.StoreAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "accounts", layout = StoreAppLayout.class)
@RolesAllowed(Role.ROLE_ADMIN)
public class UserView extends VerticalLayout {

    public UserView() {
        add(initGrid());
    }

    private Grid<AccountDTO> initGrid() {
        Grid<AccountDTO> accountGrid = new Grid<>(AccountDTO.class, false);
        accountGrid.setHeightFull();
        accountGrid.setWidthFull();
        accountGrid.addColumn(AccountDTO::id)
                .setHeader("ID")
                .setAutoWidth(true)
                .setSortable(true);
        accountGrid.addColumn(AccountDTO::uuid)
                .setHeader("UUID")
                .setAutoWidth(true)
                .setSortable(true);
        accountGrid.addColumn(AccountDTO::firstName)
                .setHeader("First Name")
                .setAutoWidth(true)
                .setSortable(true);
        accountGrid.addColumn(AccountDTO::middleName)
                .setHeader("Middle Name")
                .setAutoWidth(true)
                .setSortable(true);
        accountGrid.addColumn(AccountDTO::lastName)
                .setHeader("Last Name")
                .setAutoWidth(true)
                .setSortable(true);
        accountGrid.addColumn(AccountDTO::email)
                .setHeader("Email")
                .setAutoWidth(true)
                .setSortable(true);
        accountGrid.addColumn(AccountDTO::phone)
                .setHeader("Phone")
                .setAutoWidth(true)
                .setSortable(true);
        return accountGrid;
    }
}