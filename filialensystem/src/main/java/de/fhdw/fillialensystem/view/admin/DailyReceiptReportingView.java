package de.fhdw.fillialensystem.view.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.commons.view.AbstractMainView;
import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.entity.Receipt;
import de.fhdw.fillialensystem.persistence.entity.Register;
import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.service.ReceiptLinkArticleService;
import de.fhdw.fillialensystem.persistence.service.ReceiptService;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Route("receipt-reporting")
@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
public class DailyReceiptReportingView extends AbstractMainView {

    private final ReceiptService receiptService;
    private final ReceiptLinkArticleService receiptLinkArticleService;

    private final Grid<Receipt> receiptGrid = new Grid<>(Receipt.class, false);

    private final Checkbox dailyReceiptCheckbox = new Checkbox("Tagesabschlussrelevante");

    private final DatePicker dateFilter = new DatePicker("Filter Datum");
    private final ComboBox<Store> storeFilter = new ComboBox<>("Filter Filiale");
    private final ComboBox<Register> registerFilter = new ComboBox<>("Filter Kasse");
    private final ComboBox<Account> accountFilter = new ComboBox<>("Filter Account");

    public DailyReceiptReportingView(ReceiptService receiptService, ReceiptLinkArticleService receiptLinkArticleService) {
        this.receiptService = receiptService;
        this.receiptLinkArticleService = receiptLinkArticleService;
    }

    @Override
    protected void init() {
        receiptGrid.setItems(receiptService.findAll());

        initFilters();

        // ---- Configure Grid ----
        receiptGrid.addColumn(r -> r.getId()).setHeader("Beleg-ID").setAutoWidth(true);
        receiptGrid.addColumn(r -> r.getCreatedAt()).setHeader("Erstelldatum").setAutoWidth(true);
        receiptGrid.addColumn(r -> r.getStore().getId()).setHeader("Filiale").setAutoWidth(true);
        receiptGrid.addColumn(r -> r.getRegister().getId()).setHeader("Kasse").setAutoWidth(true);
        receiptGrid.addColumn(r -> r.getAccount().getUsername()).setHeader("Account").setAutoWidth(true);
        receiptGrid.addColumn(r -> receiptLinkArticleService.findByReceipt(r).size()).setHeader("Artikelanzahl").setAutoWidth(true);
        receiptGrid.addColumn(r -> r.getTotalAmount()).setHeader("Gesamtmenge").setAutoWidth(true);

        receiptGrid.addComponentColumn(receipt -> {
            Button actionButton = new Button("Placeholder");
            actionButton.addClickListener(e -> handleReceiptButtonClick(receipt.getId()));
            return actionButton;
        }).setHeader("Aktionen").setAutoWidth(true);

        add(receiptGrid);
        setSizeFull();
    }

    private void handleReceiptButtonClick(Long id) {
        System.out.println(id);
    }

    private void initFilters() {
        // ---- Clear Filter ----
        Button clearFilters = new Button("Filters leeren", e -> {
            dateFilter.clear();
            storeFilter.clear();
            registerFilter.clear();
            accountFilter.clear();
            filterReceipts();
        });

        // ---- Date Filter ----
        dailyReceiptCheckbox.addClickListener(e -> {
            if (dailyReceiptCheckbox.getValue()) {
                dateFilter.setEnabled(false);
                storeFilter.setEnabled(false);
                registerFilter.setEnabled(false);
                accountFilter.setEnabled(false);
                clearFilters.setEnabled(false);
            } else {
                dateFilter.setEnabled(true);
                storeFilter.setEnabled(true);
                registerFilter.setEnabled(true);
                accountFilter.setEnabled(true);
                clearFilters.setEnabled(true);
            }
        });

        // ---- Date Filter ----
        dateFilter.setPlaceholder("Wähle ein Datum");
        dateFilter.addValueChangeListener(e -> filterReceipts());

        // ---- Store Filter ----
        storeFilter.setPlaceholder("Wähle eine Filliale");
        storeFilter.setItems(receiptService.findAll().stream().map(Receipt::getStore).collect(Collectors.toSet()));
        storeFilter.setItemLabelGenerator(s -> "VKST-%d".formatted(s.getId()));
        storeFilter.addValueChangeListener(e -> filterReceipts());

        // ---- Register Filter ----
        registerFilter.setPlaceholder("Wähle eine Kasse");
        registerFilter.setItems(receiptService.findAll().stream().map(Receipt::getRegister).collect(Collectors.toSet()));
        registerFilter.setItemLabelGenerator(r -> "VKST-%d_Kasse-%d".formatted(r.getStore().getId(), r.getId()));
        registerFilter.addValueChangeListener(e -> filterReceipts());

        // ---- Account Filter ----
        accountFilter.setPlaceholder("Wähle ein Account");
        accountFilter.setItems(receiptService.findAll().stream().map(Receipt::getAccount).collect(Collectors.toSet()));
        accountFilter.setItemLabelGenerator(Account::getUsername);
        accountFilter.addValueChangeListener(e -> filterReceipts());

        HorizontalLayout filtersLayout = new HorizontalLayout(
                dailyReceiptCheckbox, dateFilter, storeFilter, registerFilter, accountFilter, clearFilters
        );

        filtersLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        filtersLayout.setSpacing(true);
        filtersLayout.setPadding(false);
        add(filtersLayout);
    }

    private void filterReceipts() {
        LocalDate selectedDate = dateFilter.getValue();
        Store selectedStore = storeFilter.getValue();
        Register selectedRegister = registerFilter.getValue();
        Account selectedAccount = accountFilter.getValue();

        receiptGrid.setItems(receiptService.findAll()
                .stream()
                .filter(r -> selectedDate == null || LocalDate.from(r.getCreatedAt()).isEqual(selectedDate))
                .filter(r -> selectedStore == null || selectedStore.equals(r.getStore()))
                .filter(r -> selectedRegister == null || selectedRegister.equals(r.getRegister()))
                .filter(r -> selectedAccount == null || selectedAccount.equals(r.getAccount()))
                .toList());
    }
}