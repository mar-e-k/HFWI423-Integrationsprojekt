package de.fhdw.fillialensystem.view.admin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.fhdw.commons.view.AbstractMainView;
import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.entity.AccountRole;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.fillialensystem.persistence.service.AccountRoleService;
import de.fhdw.fillialensystem.persistence.service.AccountService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

@Route("/roles")
@PageTitle("Roles View")
@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
public class RoleView extends AbstractMainView {

    private final AccountService accountService;
    private final AccountRoleService accountRoleService;
    private final PasswordEncoder passwordEncoder;

    private final Grid<Account> accountGrid = new Grid<>(Account.class, false);

    public RoleView(AccountService accountService, AccountRoleService accountRoleService, PasswordEncoder passwordEncoder) {
        this.accountService = accountService;
        this.accountRoleService = accountRoleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected HorizontalLayout createTopBarButtons() {
        Button homeButton = new Button("Zum Home-Screen");
        homeButton.addClickListener(e -> UI.getCurrent().navigate(""));
        return new HorizontalLayout(homeButton);
    }

    @Override
    protected void init() {
        //----------------------------------------------------------
        // FORM BEREICH
        //----------------------------------------------------------
        TextField usernameField = new TextField("Username");
        usernameField.setRequired(true);

        PasswordField passwordField = new PasswordField("Passwort");
        passwordField.setRequired(true);

        Select<AccountRoleEnum> roleSelect = new Select<>();
        roleSelect.setItems(Arrays.stream(AccountRoleEnum.values())
                .filter(role -> role != AccountRoleEnum.SYSTEM)
                .toList()
        );
        roleSelect.setLabel("Rolle");
        roleSelect.setRequiredIndicatorVisible(true);

        Button createUserBtn = new Button("Neuen Benutzer anlegen");

        FormLayout formLayout = new FormLayout(
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
        accountGrid.addColumn(Account::getUuid)
                .setHeader("Personalnummer")
                .setSortable(true)
                .setAutoWidth(true);

        accountGrid.addColumn(Account::getUsername)
                .setHeader("Username")
                .setSortable(true)
                .setAutoWidth(true);

        // Rollen-Spalte mit Bearbeitungsmöglichkeit
        accountGrid.addComponentColumn(account -> {
            Select<AccountRoleEnum> roleEditor = new Select<>();
            roleEditor.setItems(AccountRoleEnum.values());
            roleEditor.setValue(account.getAccountRole().getRole());

            Button saveButton = new Button("Speichern");
            saveButton.setVisible(false);

            roleEditor.addValueChangeListener(event -> saveButton.setVisible(true));

            saveButton.addClickListener(e -> {
                Optional<AccountRole> newRoleOpt = accountRoleService.findByRole(roleEditor.getValue());
                if (newRoleOpt.isPresent()) {
                    account.setAccountRole(newRoleOpt.get());
                    accountService.update(account.getId(), account);
                    Notification.show("Rolle aktualisiert!", 2000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    saveButton.setVisible(false);
                    refreshGrid();
                } else {
                    Notification.show("Fehler: Rolle nicht gefunden!", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });

            HorizontalLayout editorLayout = new HorizontalLayout(roleEditor, saveButton);
            editorLayout.setAlignItems(Alignment.CENTER);
            return editorLayout;
        }).setHeader("Rolle").setSortable(true).setComparator(Comparator.comparing(account -> account.getAccountRole().getRole().name())).setAutoWidth(true);


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

            if (usernameField.isEmpty()
                    || passwordField.isEmpty()
                    || roleSelect.isEmpty()) {

                Notification.show("Bitte alle Felder ausfüllen!");
                return;
            }

            Optional<AccountRole> existingRole =
                    accountRoleService.findByRole(roleSelect.getValue());

            AccountRole role = existingRole.orElseGet(() ->
                    accountRoleService.create(new AccountRole(null, roleSelect.getValue()))
            );

            Account account = new Account();
            account.setUuid(UUID.randomUUID().toString());
            account.setUsername(usernameField.getValue());
            account.setPassword(passwordEncoder.encode(passwordField.getValue()));
            account.setAccountRole(role);

            try {
                accountService.create(account);
                Notification.show("Benutzer erfolgreich angelegt!");

                // Felder leeren nach erfolgreicher Eingabe
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
