package de.fhdw.vendix.store.ui.account;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRole;
import de.fhdw.vendix.store.core.persistance.account.Account;
import de.fhdw.vendix.store.ui.StoreAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "accounts", layout = StoreAppLayout.class)
@RolesAllowed(AccountRole.ROLE_ADMIN)
public class UserView extends VerticalLayout {

    public UserView() {
        add(initGrid());
    }

    private Grid<Account> initGrid() {
        Grid<Account> accountGrid = new Grid<>(Account.class, false);
        accountGrid.setHeightFull();
        accountGrid.setWidthFull();
        accountGrid.addColumn(Account::getId)
                .setHeader("ID")
                .setAutoWidth(true)
                .setSortable(true);
        accountGrid.addColumn(Account::getUuid)
                .setHeader("UUID")
                .setAutoWidth(true)
                .setSortable(true);
        accountGrid.addColumn(Account::getFirstName)
                .setHeader("First Name")
                .setAutoWidth(true)
                .setSortable(true);
        accountGrid.addColumn(Account::getMiddleName)
                .setHeader("Middle Name")
                .setAutoWidth(true)
                .setSortable(true);
        accountGrid.addColumn(Account::getLastName)
                .setHeader("Last Name")
                .setAutoWidth(true)
                .setSortable(true);
        accountGrid.addColumn(Account::getEmail)
                .setHeader("Email")
                .setAutoWidth(true)
                .setSortable(true);
        accountGrid.addColumn(Account::getPhone)
                .setHeader("Phone")
                .setAutoWidth(true)
                .setSortable(true);
        return accountGrid;
    }
}