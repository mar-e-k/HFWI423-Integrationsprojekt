package de.fhdw.kassensystem.view.admin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.fhdw.kassensystem.persistence.entity.Account;
import de.fhdw.kassensystem.persistence.entity.AccountRoleEnum;
import de.fhdw.kassensystem.persistence.service.AccountService;
import de.fhdw.kassensystem.view.BaseView;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Route("/admin")
@PageTitle("Admin View")
@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
public class AdminView extends BaseView {

    private final AccountService accountService;
    private Grid<Account> grid;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    public AdminView(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    protected String setTopbarTitle() {
        return "Admin-Dashboard";
    }

    @Override
    protected HorizontalLayout createTopBarButtons() {
        Button roleManagementButton = new Button("Zur Rollenverwaltung");
        roleManagementButton.addClickListener(e -> UI.getCurrent().navigate("roles"));
        return new HorizontalLayout(roleManagementButton);
    }

    @Override
    protected void init() {
        // Die UI-Initialisierung wird in initUI() verschoben, um sicherzustellen,
        // dass der Service injiziert ist.
    }

    @PostConstruct
    public void initUI() {
        // Layout auf volle Größe einstellen
        setSizeFull();
        // Zentrierung aus der BaseView aufheben, damit die Komponenten sich strecken
        setAlignItems(Alignment.STRETCH);

        // Grid initialisieren und auf volle Größe einstellen
        grid = new Grid<>(Account.class, false);
        grid.setSizeFull(); // Wichtig: Grid soll den verfügbaren Platz füllen
        grid.getStyle().set("margin-top", "5em");


        // Spalten definieren
        grid.addColumn(account -> formatter.format(account.getCreatedAt())).setHeader("Erstellt am").setSortable(true);
        grid.addColumn(Account::getCreatedBy).setHeader("Erstellt von").setSortable(true);
        grid.addColumn(account -> formatter.format(account.getChangedAt())).setHeader("Geändert am").setSortable(true);
        grid.addColumn(Account::getChangedBy).setHeader("Geändert von").setSortable(true);
        grid.addColumn(Account::getAccountId).setHeader("Account ID").setSortable(true);
        grid.addColumn(Account::getUsername).setHeader("Username").setSortable(true);
        grid.addColumn(account -> account.getAccountRole().getRole().name()).setHeader("Rolle").setSortable(true);

        // Initiales Laden der Daten
        updateGrid();

        // Komponenten zum Layout hinzufügen
        add(grid);
        
        // Das Grid soll den restlichen Platz einnehmen
        setFlexGrow(1, grid);
    }

    private void updateGrid() {
        grid.setItems(accountService.findAll());
    }
}
