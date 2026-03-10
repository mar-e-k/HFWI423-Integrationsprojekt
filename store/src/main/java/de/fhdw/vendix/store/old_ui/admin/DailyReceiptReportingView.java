package de.fhdw.vendix.store.old_ui.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.theme.lumo.Lumo;
import de.fhdw.vendix.store.core.domain.account.Account;
import de.fhdw.vendix.store.core.domain.receipt.Receipt;
import de.fhdw.vendix.store.core.domain.register.Register;
import de.fhdw.vendix.store.core.domain.store.Store;
import de.fhdw.vendix.store.application.context.StoreContext;
import de.fhdw.vendix.store.application.scheduler.DailyReceiptReportingSchedule;
import de.fhdw.vendix.store.old_ui.MainView;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

//@Route("receipt-reporting")
//@PageTitle("Daily Receipt Reporting")
//@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
public class DailyReceiptReportingView
//        extends AppLayout implements BeforeEnterObserver
{

//    private final ReceiptService receiptService;
//    private final ReceiptLineService receiptLineService;
//    private final StoreContext storeContext;
//    private final DailyReceiptReportingSchedule dailyReceiptReportingSchedule;
//
//    private final Grid<Receipt> receiptGrid = new Grid<>(Receipt.class, false);
//
//    private final Checkbox dailyReceiptFilterCheckbox = new Checkbox("Tagesabschlussrelevante");
//    private final Button generateDailyReceiptButton = new Button("Tagesabschluss ausgeben");
//    private final DatePicker dateFilter = new DatePicker("Filter Datum");
//    private final ComboBox<Store> storeFilter = new ComboBox<>("Filter Filiale");
//    private final ComboBox<Register> registerFilter = new ComboBox<>("Filter Kasse");
//    private final ComboBox<Account> accountFilter = new ComboBox<>("Filter Account");
//    private final Button debugSendReportingToLogistic = new Button("[Debug] Sende Bestandsabgleich an Logistik");
//
//    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
//    private final Map<Long, Integer> articleCountCache = new HashMap<>();
//    private final List<Receipt> receipts = new ArrayList<>();
//
//    public DailyReceiptReportingView(
//            ReceiptService receiptService,
//            ReceiptLineService receiptLineService,
//            StoreContext storeContext, DailyReceiptReportingSchedule dailyReceiptReportingSchedule) {
//        this.receiptService = receiptService;
//        this.receiptLineService = receiptLineService;
//        this.storeContext = storeContext;
//        this.dailyReceiptReportingSchedule = dailyReceiptReportingSchedule;
//
//        createHeader();
//        addToDrawer(createSidebar());
//    }
//
//    private void createHeader() {
//        H1 viewTitle = new H1(getPageTitle());
//        HorizontalLayout leftSection = new HorizontalLayout(new DrawerToggle(), viewTitle);
//        leftSection.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
//
//        Span liveClockLabel = new Span();
//        liveClockLabel.setId("live-clock-label");
//        liveClockLabel.getStyle().set("font-size", "var(--lumo-font-size-l)");
//        liveClockLabel.getStyle().set("font-weight", "bold");
//
//        Button themeToggleButton = new Button(new Icon(VaadinIcon.ADJUST), click -> {
//            UI.getCurrent().getPage().executeJs("return document.documentElement.getAttribute('theme');")
//                    .then(String.class, currentClientTheme -> {
//                        var themeList = UI.getCurrent().getElement().getThemeList();
//                        boolean isClientDark = "dark".equals(currentClientTheme);
//
//                        if (isClientDark) {
//                            themeList.remove(Lumo.DARK);
//                            UI.getCurrent().getPage().executeJs("localStorage.setItem('theme', 'light');");
//                            UI.getCurrent().getPage().executeJs("document.documentElement.removeAttribute('theme');");
//                        } else {
//                            themeList.add(Lumo.DARK);
//                            UI.getCurrent().getPage().executeJs("localStorage.setItem('theme', 'dark');");
//                            UI.getCurrent().getPage().executeJs("document.documentElement.setAttribute('theme', 'dark');");
//                        }
//                    });
//        });
//        themeToggleButton.setTooltipText("Toggle dark mode");
//
//        Button logoutButton = new Button("Logout", e -> UI.getCurrent().getPage().setLocation("/logout"));
//
//        HorizontalLayout rightSection = new HorizontalLayout(liveClockLabel, themeToggleButton, logoutButton);
//        rightSection.setAlignItems(FlexComponent.Alignment.CENTER);
//        rightSection.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
//        rightSection.setSpacing(true);
//
//        HorizontalLayout header = new HorizontalLayout(leftSection, rightSection);
//        header.setWidthFull();
//        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
//        header.setAlignItems(FlexComponent.Alignment.CENTER);
//
//        addToNavbar(header);
//
//        UI.getCurrent().getPage().executeJs("""
//            const label = document.getElementById('live-clock-label');
//            if (label) {
//                setInterval(() => {
//                    const now = new Date();
//                    label.textContent = now.toLocaleString('de-DE', {
//                        year: 'numeric', month: '2-digit', day: '2-digit',
//                        hour: '2-digit', minute: '2-digit', second: '2-digit'
//                    });
//                }, 1000);
//            }
//        """);
//    }
//
//    private Component createSidebar() {
//        VerticalLayout sidebar = new VerticalLayout();
//        sidebar.setPadding(false);
//        sidebar.setSpacing(false);
//        sidebar.setAlignItems(FlexComponent.Alignment.STRETCH);
//
//        sidebar.add(
//                createSidebarLink("Home", VaadinIcon.HOME, MainView.class),
//                createSidebarLink("Admin View", VaadinIcon.USER, AdminView.class),
//                createSidebarLink("Role View", VaadinIcon.GROUP, RoleView.class),
//                createSidebarLink("Register View", VaadinIcon.CASH, RegisterAddView.class),
//                createSidebarLink("Stock View", VaadinIcon.PACKAGE, StockView.class),
//                createSidebarLink("Daily Receipt Reporting", VaadinIcon.RECORDS, DailyReceiptReportingView.class)
//        );
//        return sidebar;
//    }
//
//    private RouterLink createSidebarLink(String text, VaadinIcon icon, Class<? extends Component> navigationTarget) {
//        com.vaadin.flow.component.icon.Icon i = new com.vaadin.flow.component.icon.Icon(icon);
//        Span textSpan = new Span(text);
//        textSpan.getStyle().set("margin-left", "var(--lumo-space-m)");
//
//        RouterLink link = new RouterLink(navigationTarget);
//        link.add(i, textSpan);
//        link.getStyle().set("display", "flex");
//        link.getStyle().set("align-items", "center");
//        link.getStyle().set("padding", "var(--lumo-space-s)");
//        link.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
//        link.getStyle().set("transition", "background-color 0.2s");
//        link.getStyle().set("text-decoration", "none");
//        link.getStyle().set("color", "var(--lumo-body-text-color)");
//
//        return link;
//    }
//
//    @PostConstruct
//    public void initUI() {
//        VerticalLayout content = new VerticalLayout();
//        content.setSizeFull();
//        content.setPadding(true);
//        content.setSpacing(true);
//        content.setAlignItems(FlexComponent.Alignment.STRETCH);
//
//        receipts.addAll(receiptService.findAll());
//        receiptGrid.setItems(receipts);
//
//        initButtons();
//        initFilters();
//        configureGrid();
//
//        content.add(getFiltersLayout(), receiptGrid);
//        content.setFlexGrow(1, receiptGrid); // Grid soll den restlichen Platz einnehmen
//
//        setContent(content);
//    }
//
//    private HorizontalLayout getFiltersLayout() {
//        Button clearFilters = new Button("Filter löschen", e -> {
//            dailyReceiptFilterCheckbox.setValue(false);
//            clearManualFilters();
//            filterReceipts();
//        });
//
//        dailyReceiptFilterCheckbox.addValueChangeListener(check -> {
//            if (check.getValue()) {
//                dateFilter.setValue(LocalDate.now());
//                storeFilter.setValue(storeContext.getStore());
//                setManualFiltersEnabled(false);
//            } else {
//                setManualFiltersEnabled(true);
//                clearManualFilters();
//            }
//            filterReceipts();
//        });
//
//        dateFilter.addValueChangeListener(e -> filterReceipts());
//
//        storeFilter.setItems(distinctStores());
//        storeFilter.setItemLabelGenerator(s -> "VKST-" + s.getId());
//        storeFilter.addValueChangeListener(e -> filterReceipts());
//
//        registerFilter.setItems(distinctRegisters());
//        registerFilter.setItemLabelGenerator(r -> "VKST-%d_Kasse-%d".formatted(r.getStore().getId(), r.getId()));
//        registerFilter.addValueChangeListener(e -> filterReceipts());
//
//        accountFilter.setItems(distinctAccounts());
//        accountFilter.setItemLabelGenerator(Account::getUsername);
//        accountFilter.addValueChangeListener(e -> filterReceipts());
//
//        HorizontalLayout filtersLayout = new HorizontalLayout(
//                new VerticalLayout(dailyReceiptFilterCheckbox, generateDailyReceiptButton),
//                dateFilter,
//                storeFilter,
//                registerFilter,
//                accountFilter,
//                clearFilters,
//                debugSendReportingToLogistic
//        );
//
//        filtersLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
//        filtersLayout.setSpacing(true);
//        filtersLayout.setPadding(false);
//        return filtersLayout;
//    }
//
//    private void initButtons() {
//        generateDailyReceiptButton.addClickListener(c -> handleGenerateDailyReceiptButtonClick());
//        debugSendReportingToLogistic.addClickListener(c -> dailyReceiptReportingSchedule.sendDailyReceiptReport());
//    }
//
//    private void initFilters() {
//        // Filters are now created in getFiltersLayout() and added to content in initUI()
//    }
//
//    private void configureGrid() {
//        receiptGrid.addColumn(Receipt::getId)
//                .setHeader("Beleg-ID")
//                .setAutoWidth(true)
//                .setSortable(true);
//
//        receiptGrid.addColumn(r -> r.getCreatedAt().atZone(ZoneId.systemDefault()).format(fmt))
//                .setHeader("Erstelldatum")
//                .setAutoWidth(true)
//                .setSortable(true);
//        receiptGrid.addColumn(r -> r.getStore().getId())
//                .setHeader("Filiale")
//                .setAutoWidth(true)
//                .setSortable(true);
//        receiptGrid.addColumn(r -> r.getRegister().getId())
//                .setHeader("Kasse")
//                .setAutoWidth(true)
//                .setSortable(true);
//        receiptGrid.addColumn(r -> r.getCashier().getUsername())
//                .setHeader("Account")
//                .setAutoWidth(true)
//                .setSortable(true);
//        receiptGrid.addColumn(r -> articleCountCache.computeIfAbsent(
//                        r.getId(),
//                        id -> receiptLineService.findByReceipt(r).size()))
//                .setHeader("Artikelanzahl")
//                .setAutoWidth(true)
//                .setSortable(true);
//        receiptGrid.addColumn(Receipt::getTotalAmount)
//                .setHeader("Gesamtpreis")
//                .setAutoWidth(true)
//                .setSortable(true);
//        receiptGrid.addComponentColumn(receipt -> {
//            Button printButton = new Button("Beleg drucken");
//            printButton.addClickListener(e -> handleReceiptButtonClick(receipt.getId()));
//            return printButton;
//        }).setHeader("Aktionen");
//    }
//
//    private void handleReceiptButtonClick(Long receiptId) {
//        ByteArrayInputStream generatedPdfStream = receiptService.generateReceipt(receiptId, false);
//        byte[] pdfBytes = generatedPdfStream.readAllBytes();
//
//        String fileName = "Bon-%d-%s.pdf".formatted(receiptId, LocalDate.now());
//
//        DownloadHandler handler = DownloadHandler.fromInputStream(event ->
//                new DownloadResponse(
//                        new ByteArrayInputStream(pdfBytes),
//                        fileName,
//                        "application/pdf",
//                        pdfBytes.length
//                )
//        );
//
//        Anchor a = new Anchor(handler, "");
//        a.getElement().setAttribute("download", fileName);
//        a.getElement().setAttribute("style", "display:none");
//
//        UI.getCurrent().add(a); // Changed from add(a) to UI.getCurrent().add(a)
//        a.getElement().callJsFunction("click");
//        a.getElement().executeJs("this.remove()");
//    }
//
//    private void handleGenerateDailyReceiptButtonClick() {
//        List<Receipt> receipts = receiptService.findAll().stream()
//                .filter(r -> r.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate().equals(LocalDate.now()))
//                .filter(r -> r.getStore().equals(storeContext.getStore()))
//                .toList();
//
//        ByteArrayInputStream generatedPdfStream = receiptService.generateDailyReceipt(receipts);
//        byte[] pdfBytes = generatedPdfStream.readAllBytes();
//
//        String fileName = "Tagesabschluss-%d-%s.pdf".formatted(storeContext.getStore().getId(), LocalDate.now());
//
//        DownloadHandler handler = DownloadHandler.fromInputStream(event ->
//                new DownloadResponse(
//                        new ByteArrayInputStream(pdfBytes),
//                        fileName,
//                        "application/pdf",
//                        pdfBytes.length
//                )
//        );
//
//        Anchor a = new Anchor(handler, "");
//        a.getElement().setAttribute("download", fileName);
//        a.getElement().setAttribute("style", "display:none");
//
//        UI.getCurrent().add(a); // Changed from add(a) to UI.getCurrent().add(a)
//        a.getElement().callJsFunction("click");
//        a.getElement().executeJs("this.remove()");
//    }
//
//    private Set<Store> distinctStores() {
//        return receipts.stream().map(Receipt::getStore).collect(Collectors.toSet());
//    }
//
//    private Set<Register> distinctRegisters() {
//        return receipts.stream().map(Receipt::getRegister).collect(Collectors.toSet());
//    }
//
//    private Set<Account> distinctAccounts() {
//        return receipts.stream().map(Receipt::getCashier).collect(Collectors.toSet());
//    }
//
//    private void setManualFiltersEnabled(boolean enabled) {
//        dateFilter.setEnabled(enabled);
//        storeFilter.setEnabled(enabled);
//        registerFilter.setEnabled(enabled);
//        accountFilter.setEnabled(enabled);
//    }
//
//    private void clearManualFilters() {
//        dateFilter.clear();
//        storeFilter.clear();
//        registerFilter.clear();
//        accountFilter.clear();
//    }
//
//    private void filterReceipts() {
//
//        LocalDate selectedDate = dateFilter.getValue();
//        Store selectedStore = storeFilter.getValue();
//        Register selectedRegister = registerFilter.getValue();
//        Account selectedAccount = accountFilter.getValue();
//
//        List<Receipt> filtered = receipts.stream()
//                .filter(r -> selectedDate == null ||
//                        selectedDate.equals(r.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()))
//                .filter(r -> selectedStore == null || selectedStore.equals(r.getStore()))
//                .filter(r -> selectedRegister == null || selectedRegister.equals(r.getRegister()))
//                .filter(r -> selectedAccount == null || selectedAccount.equals(r.getCashier()))
//                .toList();
//
//        receiptGrid.setItems(filtered);
//    }
//
//    private String getPageTitle() {
//        PageTitle titleAnnotation = this.getClass().getAnnotation(PageTitle.class);
//        return titleAnnotation != null ? titleAnnotation.value() : "Daily Receipt Reporting";
//    }
//
//    @Override
//    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
//        Class<?> targetView = beforeEnterEvent.getNavigationTarget();
//
//        RolesAllowed rolesAllowed = targetView.getAnnotation(RolesAllowed.class);
//        if (rolesAllowed == null) {
//            return;
//        }
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
//            beforeEnterEvent.rerouteTo("login");
//            return;
//        }
//
//        Set<String> userAuthorities = authentication.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toSet());
//
//        boolean authorized = Arrays.stream(rolesAllowed.value())
//                .anyMatch(userAuthorities::contains);
//
//        if (!authorized) {
//            beforeEnterEvent.rerouteTo("login");
//        }
//    }
}