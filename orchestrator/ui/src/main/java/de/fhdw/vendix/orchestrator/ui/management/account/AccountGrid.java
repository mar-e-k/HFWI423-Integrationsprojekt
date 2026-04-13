package de.fhdw.vendix.orchestrator.ui.management.account;

import de.fhdw.vendix.commons.spring.vaadin.layout.AbstractLazySortableGrid;
import de.fhdw.vendix.orchestrator.core.domain.account.Account;
import de.fhdw.vendix.orchestrator.core.domain.account.AccountService;

public final class AccountGrid extends AbstractLazySortableGrid<Account> {

    public AccountGrid(AccountService service) {
        super(Account.class, service);
    }

    @Override
    protected void configureColumns() {
        addColumn(Account::getId)
                .setHeader("ID")
                .setKey("id")
                .setSortable(true);
        addColumn(Account::getUuid)
                .setHeader("UUID")
                .setSortable(true);
        addColumn(Account::getUsername)
                .setHeader("Username")
                .setSortable(true);
        addColumn(Account::getFirstName)
                .setHeader("First Name")
                .setSortable(true);
        addColumn(Account::getMiddleName)
                .setHeader("Middle Name")
                .setSortable(true);
        addColumn(Account::getLastName)
                .setHeader("Last Name")
                .setSortable(true);
        addColumn(Account::getEmail)
                .setHeader("Email")
                .setSortable(true);
        addColumn(Account::getPhone)
                .setHeader("Phone")
                .setSortable(true);
    }
}