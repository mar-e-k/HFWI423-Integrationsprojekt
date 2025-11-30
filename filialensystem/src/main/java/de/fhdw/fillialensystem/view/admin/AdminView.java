package de.fhdw.fillialensystem.view.admin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.fhdw.commons.api.dto.LogisticMessageDTO;
import de.fhdw.commons.api.rabbitmq.DomainCommand;
import de.fhdw.commons.api.rabbitmq.DomainQueue;
import de.fhdw.commons.view.AbstractMainView;
import de.fhdw.fillialensystem.api.rabbitmq.CommandSender;
import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.fillialensystem.persistence.service.AccountService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Route("/admin")
@PageTitle("Admin View")
@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
public class AdminView extends AbstractMainView {

    private final AccountService accountService;
    private final CommandSender<LogisticMessageDTO> commandSender;

    private Grid<Account> grid;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    public AdminView(AccountService accountService, CommandSender<LogisticMessageDTO> commandSender) {
        this.accountService = accountService;
        this.commandSender = commandSender;
    }

    @Override
    protected HorizontalLayout createTopBarButtons() {
        Button roleManagementButton = new Button("Zur Rollenverwaltung");
        roleManagementButton.addClickListener(e -> UI.getCurrent().navigate("roles"));

        LogisticMessageDTO logisticMessageDTO = new LogisticMessageDTO();
        logisticMessageDTO.setStoreId(2L);
        logisticMessageDTO.setArticleId(1L);
        logisticMessageDTO.setQuantity(10L);

        Button rabbitMQButton = new Button("Logistik TestNachricht");
        rabbitMQButton.addClickListener(e -> {
            commandSender.fire(DomainQueue.LOGISTIC_STORE_RESTOCK,
                    DomainCommand.STORE_RESTOCK,
                    logisticMessageDTO);});

        return new HorizontalLayout(roleManagementButton,  rabbitMQButton);
    }

    @Override
    protected void init() {
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
        grid.addColumn(Account::getUuid).setHeader("Account ID").setSortable(true);
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
