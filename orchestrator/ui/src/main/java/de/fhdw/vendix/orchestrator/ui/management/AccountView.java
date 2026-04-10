package de.fhdw.vendix.orchestrator.ui.management;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.data.binder.Binder;

import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.orchestrator.core.domain.account.Account;
import de.fhdw.vendix.orchestrator.core.domain.account.AccountService;
import de.fhdw.vendix.orchestrator.ui.OrchestratorAppLayout;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "account", layout = OrchestratorAppLayout.class)
@RolesAllowed(Role.ROLE_ADMIN)
public class AccountView extends Div {

    private final AccountService accountService;

    private Grid<Account> accountGrid;

    private final FormLayout accountDetailsFormLayout = new FormLayout();

    private final TextField createdAt = new TextField("Created At");
    private final TextField createdBy = new TextField("Created By");
    private final TextField changedAt = new TextField("Changed At");
    private final TextField changedBy = new TextField("Changed By");

    private final Binder<Account> binder = new Binder<>(Account.class);

    public AccountView(AccountService accountService) {
        this.accountService = accountService;

        setSizeFull();

        initGrid();
        initDetailFormLayout();

        MasterDetailLayout masterDetailLayout = new MasterDetailLayout();
        masterDetailLayout.setSizeFull();
        masterDetailLayout.setMaster(accountGrid);
        masterDetailLayout.setDetail(accountDetailsFormLayout);
        masterDetailLayout.setDetailSize("50rem");
        masterDetailLayout.addBackdropClickListener(event -> {
            masterDetailLayout.setDetail(null);
        });
        masterDetailLayout.addDetailEscapePressListener(event -> {
            masterDetailLayout.setDetail(null);
        });
        add(masterDetailLayout);
    }

    private void initGrid() {
        accountGrid = new Grid<>(Account.class, false);
        accountGrid.setSizeFull();
        accountGrid.addColumn(Account::getId)
                .setHeader("ID")
                .setAutoWidth(true)
                .setSortable(true);
        accountGrid.addColumn(Account::getUuid)
                .setHeader("UUID")
                .setAutoWidth(true)
                .setSortable(true);
        accountGrid.addColumn(Account::getPassword)
                .setHeader("Password (Hashed)")
                .setAutoWidth(true)
                .setSortable(true);
        accountGrid.addColumn(Account::getUsername)
                .setHeader("Username")
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
        accountGrid.setItems(accountService.findAll());

        accountGrid.asSingleSelect().addValueChangeListener(event -> {
            Account selected = event.getValue();
            if (selected != null) {
                binder.setBean(selected);
                accountDetailsFormLayout.setVisible(true);
            } else {
                binder.setBean(null);
                accountDetailsFormLayout.setVisible(false);
            }
        });
    }

    private void initDetailFormLayout() {
        accountDetailsFormLayout.setWidthFull();
        accountDetailsFormLayout.setVisible(false);

        createdAt.setReadOnly(true);
        createdBy.setReadOnly(true);
        changedAt.setReadOnly(true);
        changedBy.setReadOnly(true);

        binder.forField(createdAt)
                .bindReadOnly(acc -> String.valueOf(acc.getCreatedAt()));

        binder.forField(createdBy)
                .bindReadOnly(acc -> String.valueOf(acc.getCreatedBy()));

        binder.forField(changedAt)
                .bindReadOnly(acc -> String.valueOf(acc.getChangedAt()));

        binder.forField(changedBy)
                .bindReadOnly(acc -> String.valueOf(acc.getChangedBy()));

        Button closeButton = new Button("Close");
        closeButton.addClickListener(_ -> {
            accountGrid.deselectAll();
            accountDetailsFormLayout.setVisible(false);
        });

        accountDetailsFormLayout.addFormRow(createdAt, createdBy);
        accountDetailsFormLayout.addFormRow(changedAt, changedBy);
        accountDetailsFormLayout.add(closeButton);
    }
}