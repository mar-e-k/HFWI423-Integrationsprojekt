package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.fhdw.kassensystem.persistence.entity.Account;
import de.fhdw.kassensystem.persistence.entity.AccountRole;
import de.fhdw.kassensystem.persistence.entity.AccountRoleEnum;
import de.fhdw.kassensystem.persistence.service.AccountRoleService;
import de.fhdw.kassensystem.persistence.service.AccountService;
import jakarta.annotation.security.RolesAllowed;

import java.util.Optional;

@Route("/role")
@PageTitle("Roles View")
@RolesAllowed({AccountRoleEnum.ROLE_CASHIER, AccountRoleEnum.ROLE_ADMIN})
public class RoleView extends BaseView {

    private final AccountService accountService;
    private final AccountRoleService accountRoleService;

    private final Grid<Account> accountGrid = new Grid<>(Account.class, false);

    public RoleView(AccountService accountService, AccountRoleService accountRoleService) {
        this.accountService = accountService;
        this.accountRoleService = accountRoleService;
    }

    @Override
    protected String setTopbarTitle() {
        return "Role Management";
    }

    @Override
    protected void init() {
        //----------------------------------------------------------
        // FORM BEREICH
        //----------------------------------------------------------
        TextField accountIdField = new TextField("Personalnummer");
        accountIdField.setRequired(true);

        TextField usernameField = new TextField("Username");
        usernameField.setRequired(true);

        PasswordField passwordField = new PasswordField("Passwort");
        passwordField.setRequired(true);

        Select<AccountRoleEnum> roleSelect = new Select<>();
        roleSelect.setItems(AccountRoleEnum.values());
        roleSelect.setLabel("Rolle");
        roleSelect.setRequiredIndicatorVisible(true);

        Button createUserBtn = new Button("Neuen Benutzer anlegen");

        FormLayout formLayout = new FormLayout(
                accountIdField,
                usernameField,
                passwordField,
                roleSelect,
                createUserBtn
        );

        VerticalLayout wrapper = new VerticalLayout(formLayout);
        wrapper.setWidth("400px");
        add(wrapper);

        //----------------------------------------------------------
        // TABELLE MIT ACCOUNTS
        //----------------------------------------------------------
        accountGrid.addColumn(Account::getAccountId)
                .setHeader("Personalnummer")
                .setAutoWidth(true);

        accountGrid.addColumn(Account::getUsername)
                .setHeader("Username")
                .setAutoWidth(true);

        accountGrid.addColumn(acc -> acc.getAccountRole().getRole().name())
                .setHeader("Rolle")
                .setAutoWidth(true);

        // Löschen-Button-Spalte
        accountGrid.addComponentColumn(account -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.getElement().setProperty("title", "Benutzer löschen");

            deleteButton.addClickListener(e -> {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setHeader("Benutzer löschen?");
                dialog.setText("Möchten Sie den Benutzer '" + account.getUsername() + "' wirklich löschen?");
                dialog.setConfirmText("Ja");
                dialog.setCancelText("Nein");
                dialog.addConfirmListener(event -> {
                    try {
                        // Hier ID statt Objekt übergeben
                        accountService.delete(account.getId());
                        Notification.show("Benutzer erfolgreich gelöscht!", 2000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        refreshGrid();
                    } catch (Exception ex) {
                        Notification.show("Fehler beim Löschen: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
                dialog.open();
            });

            return deleteButton;
        }).setHeader("Löschen").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);

        refreshGrid();
        add(accountGrid);

        //----------------------------------------------------------
        // EVENT: BENUTZER ANLEGEN
        //----------------------------------------------------------
        createUserBtn.addClickListener(e -> {

            if (accountIdField.isEmpty()
                    || usernameField.isEmpty()
                    || passwordField.isEmpty()
                    || roleSelect.isEmpty()) {

                Notification.show("Bitte alle Felder ausfüllen!");
                return;
            }

            int personalNumber;
            try {
                personalNumber = Integer.parseInt(accountIdField.getValue());
            } catch (NumberFormatException ex) {
                Notification.show("Personalnummer muss eine Zahl sein!");
                return;
            }

            Optional<AccountRole> existingRole =
                    accountRoleService.findByRole(roleSelect.getValue());

            AccountRole role = existingRole.orElseGet(() ->
                    accountRoleService.create(new AccountRole(null, roleSelect.getValue()))
            );

            Account account = new Account();
            account.setAccountId(personalNumber);
            account.setUsername(usernameField.getValue());
            account.setPassword(passwordField.getValue());
            account.setAccountRole(role);

            try {
                accountService.create(account);
                Notification.show("Benutzer erfolgreich angelegt!");

                // Felder leeren nach erfolgreicher Eingabe
                accountIdField.clear();
                usernameField.clear();
                passwordField.clear();
                roleSelect.clear();

                refreshGrid();

            } catch (Exception ex) {
                Notification.show("Fehler beim Anlegen: " + ex.getMessage());
            }
        });
    }

    private void refreshGrid() {
        accountGrid.setItems(accountService.findAll());
    }
}
