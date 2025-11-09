package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.select.Select;
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

        // --- Eingabefelder ---
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

        // --- Layout ---
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

        // --- Event: Benutzer anlegen ---
        createUserBtn.addClickListener(e -> {

            // Validierung
            if (accountIdField.isEmpty()
                    || usernameField.isEmpty()
                    || passwordField.isEmpty()
                    || roleSelect.isEmpty()) {

                Notification.show("Bitte alle Felder ausfüllen!");
                return;
            }

            // Personalnummer validieren
            int personalNumber;
            try {
                personalNumber = Integer.parseInt(accountIdField.getValue());
            } catch (NumberFormatException ex) {
                Notification.show("Personalnummer muss eine Zahl sein!");
                return;
            }

            // AccountRole holen oder anlegen
            Optional<AccountRole> existingRole =
                    accountRoleService.findByRole(roleSelect.getValue());

            AccountRole role = existingRole.orElseGet(() ->
                    accountRoleService.create(new AccountRole(null, roleSelect.getValue()))
            );

            // Account erstellen
            Account account = new Account();
            account.setAccountId(personalNumber);             // Personalnummer setzen
            account.setUsername(usernameField.getValue());
            account.setPassword(passwordField.getValue());    // später verschlüsseln!
            account.setAccountRole(role);

            try {
                accountService.create(account);
                Notification.show("Benutzer erfolgreich angelegt!");

                // Felder leeren
                accountIdField.clear();
                usernameField.clear();
                passwordField.clear();
                roleSelect.clear();

            } catch (Exception ex) {
                Notification.show("Fehler beim Anlegen: " + ex.getMessage());
            }
        });
    }
}
