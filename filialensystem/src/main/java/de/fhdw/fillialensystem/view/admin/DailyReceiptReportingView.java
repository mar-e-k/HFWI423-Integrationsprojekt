package de.fhdw.fillialensystem.view.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.commons.view.AbstractMainView;
import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.entity.Receipt;
import de.fhdw.fillialensystem.persistence.entity.Register;
import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.service.ReceiptLinkArticleService;
import de.fhdw.fillialensystem.persistence.service.ReceiptService;
import de.fhdw.fillialensystem.utility.StoreClient;
import de.fhdw.fillialensystem.utility.scheduler.DailyReceiptReportingSchedule;
import jakarta.annotation.security.RolesAllowed;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route("receipt-reporting")
@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
public class DailyReceiptReportingView extends AbstractMainView {

    private final ReceiptService receiptService;
    private final ReceiptLinkArticleService receiptLinkArticleService;
    private final StoreClient storeClient;
    private final DailyReceiptReportingSchedule dailyReceiptReportingSchedule;

    private final Grid<Receipt> receiptGrid = new Grid<>(Receipt.class, false);

    private final Checkbox dailyReceiptFilterCheckbox = new Checkbox("Tagesabschlussrelevante");
    private final Button generateDailyReceiptButton = new Button("Tagesabschluss ausgeben");
    private final DatePicker dateFilter = new DatePicker("Filter Datum");
    private final ComboBox<Store> storeFilter = new ComboBox<>("Filter Filiale");
    private final ComboBox<Register> registerFilter = new ComboBox<>("Filter Kasse");
    private final ComboBox<Account> accountFilter = new ComboBox<>("Filter Account");
    private final Button debugSendReportingToLogistic = new Button("[Debug] Sende Bestandsabgleich an Logistik");

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final Map<Long, Integer> articleCountCache = new HashMap<>();
    private final List<Receipt> receipts = new ArrayList<>();

    public DailyReceiptReportingView(
            ReceiptService receiptService,
            ReceiptLinkArticleService receiptLinkArticleService,
            StoreClient storeClient, DailyReceiptReportingSchedule dailyReceiptReportingSchedule) {
        this.receiptService = receiptService;
        this.receiptLinkArticleService = receiptLinkArticleService;
        this.storeClient = storeClient;
        this.dailyReceiptReportingSchedule = dailyReceiptReportingSchedule;
    }

    @Override
    protected void init() {
        receipts.addAll(receiptService.findAll());
        receiptGrid.setItems(receipts);

        initButtons();
        initFilters();
        configureGrid();

        add(receiptGrid);
        setSizeFull();
    }

    private void initButtons() {
        generateDailyReceiptButton.addClickListener(c -> handleGenerateDailyReceiptButtonClick());
        debugSendReportingToLogistic.addClickListener(c -> dailyReceiptReportingSchedule.sendDailyReceiptReport());
    }

    private void initFilters() {
        Button clearFilters = new Button("Filter löschen", e -> {
            dailyReceiptFilterCheckbox.setValue(false);
            clearManualFilters();
            filterReceipts();
        });

        dailyReceiptFilterCheckbox.addValueChangeListener(check -> {
            if (check.getValue()) {
                dateFilter.setValue(LocalDate.now());
                storeFilter.setValue(storeClient.getStore());
                setManualFiltersEnabled(false);
            } else {
                setManualFiltersEnabled(true);
                clearManualFilters();
            }
            filterReceipts();
        });

        dateFilter.addValueChangeListener(e -> filterReceipts());

        storeFilter.setItems(distinctStores());
        storeFilter.setItemLabelGenerator(s -> "VKST-" + s.getId());
        storeFilter.addValueChangeListener(e -> filterReceipts());

        registerFilter.setItems(distinctRegisters());
        registerFilter.setItemLabelGenerator(r -> "VKST-%d_Kasse-%d".formatted(r.getStore().getId(), r.getId()));
        registerFilter.addValueChangeListener(e -> filterReceipts());

        accountFilter.setItems(distinctAccounts());
        accountFilter.setItemLabelGenerator(Account::getUsername);
        accountFilter.addValueChangeListener(e -> filterReceipts());

        HorizontalLayout filtersLayout = new HorizontalLayout(
                new VerticalLayout(dailyReceiptFilterCheckbox, generateDailyReceiptButton),
                dateFilter,
                storeFilter,
                registerFilter,
                accountFilter,
                clearFilters,
                debugSendReportingToLogistic
        );

        filtersLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        filtersLayout.setSpacing(true);
        filtersLayout.setPadding(false);
        add(filtersLayout);
    }

    private void configureGrid() {
        receiptGrid.addColumn(Receipt::getId)
                .setHeader("Beleg-ID")
                .setAutoWidth(true)
                .setSortable(true);

        receiptGrid.addColumn(r -> r.getCreatedAt().atZone(ZoneId.systemDefault()).format(fmt))
                .setHeader("Erstelldatum")
                .setAutoWidth(true)
                .setSortable(true);
        receiptGrid.addColumn(r -> r.getStore().getId())
                .setHeader("Filiale")
                .setAutoWidth(true)
                .setSortable(true);
        receiptGrid.addColumn(r -> r.getRegister().getId())
                .setHeader("Kasse")
                .setAutoWidth(true)
                .setSortable(true);
        receiptGrid.addColumn(r -> r.getAccount().getUsername())
                .setHeader("Account")
                .setAutoWidth(true)
                .setSortable(true);
        receiptGrid.addColumn(r -> articleCountCache.computeIfAbsent(
                        r.getId(),
                        id -> receiptLinkArticleService.findByReceipt(r).size()))
                .setHeader("Artikelanzahl")
                .setAutoWidth(true)
                .setSortable(true);
        receiptGrid.addColumn(Receipt::getTotalAmount)
                .setHeader("Gesamtpreis")
                .setAutoWidth(true)
                .setSortable(true);
        receiptGrid.addComponentColumn(receipt -> {
            Button printButton = new Button("Beleg drucken");
            printButton.addClickListener(e -> handleReceiptButtonClick(receipt.getId()));
            return printButton;
        }).setHeader("Aktionen");
    }

    private void handleReceiptButtonClick(Long receiptId) {
        ByteArrayInputStream generatedPdfStream = receiptService.generateReceipt(receiptId, false);
        byte[] pdfBytes = generatedPdfStream.readAllBytes();

        String fileName = "Bon-%d-%s.pdf".formatted(receiptId, LocalDate.now());

        DownloadHandler handler = DownloadHandler.fromInputStream(event ->
                new DownloadResponse(
                        new ByteArrayInputStream(pdfBytes),
                        fileName,
                        "application/pdf",
                        pdfBytes.length
                )
        );

        Anchor a = new Anchor(handler, "");
        a.getElement().setAttribute("download", fileName);
        a.getElement().setAttribute("style", "display:none");

        add(a);
        a.getElement().callJsFunction("click");
        a.getElement().executeJs("this.remove()");
    }

    private void handleGenerateDailyReceiptButtonClick() {
        List<Receipt> receipts = receiptService.findAll().stream()
                .filter(r -> r.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate().equals(LocalDate.now()))
                .filter(r -> r.getStore().equals(storeClient.getStore()))
                .toList();

        ByteArrayInputStream generatedPdfStream = receiptService.generateDailyReceipt(receipts);
        byte[] pdfBytes = generatedPdfStream.readAllBytes();

        String fileName = "Tagesabschluss-%d-%s.pdf".formatted(storeClient.getStore().getId(), LocalDate.now());

        DownloadHandler handler = DownloadHandler.fromInputStream(event ->
                new DownloadResponse(
                        new ByteArrayInputStream(pdfBytes),
                        fileName,
                        "application/pdf",
                        pdfBytes.length
                )
        );

        Anchor a = new Anchor(handler, "");
        a.getElement().setAttribute("download", fileName);
        a.getElement().setAttribute("style", "display:none");

        add(a);
        a.getElement().callJsFunction("click");
        a.getElement().executeJs("this.remove()");
    }

    private Set<Store> distinctStores() {
        return receipts.stream().map(Receipt::getStore).collect(Collectors.toSet());
    }

    private Set<Register> distinctRegisters() {
        return receipts.stream().map(Receipt::getRegister).collect(Collectors.toSet());
    }

    private Set<Account> distinctAccounts() {
        return receipts.stream().map(Receipt::getAccount).collect(Collectors.toSet());
    }

    private void setManualFiltersEnabled(boolean enabled) {
        dateFilter.setEnabled(enabled);
        storeFilter.setEnabled(enabled);
        registerFilter.setEnabled(enabled);
        accountFilter.setEnabled(enabled);
    }

    private void clearManualFilters() {
        dateFilter.clear();
        storeFilter.clear();
        registerFilter.clear();
        accountFilter.clear();
    }

    private void filterReceipts() {

        LocalDate selectedDate = dateFilter.getValue();
        Store selectedStore = storeFilter.getValue();
        Register selectedRegister = registerFilter.getValue();
        Account selectedAccount = accountFilter.getValue();

        List<Receipt> filtered = receipts.stream()
                .filter(r -> selectedDate == null ||
                        selectedDate.equals(r.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()))
                .filter(r -> selectedStore == null || selectedStore.equals(r.getStore()))
                .filter(r -> selectedRegister == null || selectedRegister.equals(r.getRegister()))
                .filter(r -> selectedAccount == null || selectedAccount.equals(r.getAccount()))
                .toList();

        receiptGrid.setItems(filtered);
    }
}
