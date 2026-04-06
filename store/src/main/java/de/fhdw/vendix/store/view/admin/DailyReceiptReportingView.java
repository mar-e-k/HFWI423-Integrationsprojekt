package de.fhdw.vendix.store.view.admin;

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
import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import de.fhdw.vendix.store.persistence.entity.Account;
import de.fhdw.vendix.store.persistence.entity.Receipt;
import de.fhdw.vendix.store.persistence.entity.Register;
import de.fhdw.vendix.store.persistence.entity.Store;
import de.fhdw.vendix.store.persistence.service.ReceiptLinkArticleService;
import de.fhdw.vendix.store.persistence.service.ReceiptService;
import de.fhdw.vendix.store.utility.StoreClient;
import de.fhdw.vendix.store.utility.scheduler.DailyReceiptReportingSchedule;
import de.fhdw.vendix.store.view.MainView;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route("receipt-reporting")
@PageTitle("Daily Receipt Reporting")
@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
public class DailyReceiptReportingView extends AppLayout implements BeforeEnterObserver {

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

        createHeader();
        addToDrawer(createSidebar());
    }

    private void createHeader() {
        H1 viewTitle = new H1(getPageTitle());
        viewTitle.getStyle().set("margin", "0");
        viewTitle.getStyle().set("color", "var(--vaadin-primary-color, var(--lumo-primary-color))");
        viewTitle.getStyle().set("font-size", "1.5rem");

        HorizontalLayout leftSection = new HorizontalLayout(new DrawerToggle(), viewTitle);
        leftSection.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        leftSection.setSpacing(true);

        Span liveClockLabel = new Span();
        liveClockLabel.setId("live-clock-label");
        liveClockLabel.getStyle().set("font-size", "var(--lumo-font-size-m)");
        liveClockLabel.getStyle().set("font-weight", "500");
        liveClockLabel.getStyle().set("color", "var(--vaadin-secondary-text-color, var(--lumo-secondary-text-color))");

        Button themeToggleButton = new Button(new Icon(VaadinIcon.MOON), click -> {
            UI ui = UI.getCurrent();
            ui.getPage().executeJs("""
                const html = document.documentElement;
                const current = getComputedStyle(html).colorScheme;
                const isDark = current.includes('dark');
                html.style.colorScheme = isDark ? 'light' : 'dark';
                return !isDark;
            """).then(Boolean.class, isDarkNow -> {
                ((Button) click.getSource()).setIcon(new Icon(isDarkNow ? VaadinIcon.SUN_O : VaadinIcon.MOON));
            });
        });
        themeToggleButton.setTooltipText("Dark Mode umschalten");
        themeToggleButton.getStyle().set("border-radius", "var(--lumo-border-radius-m)");

        Button logoutButton = new Button("Logout", new Icon(VaadinIcon.SIGN_OUT), e -> UI.getCurrent().getPage().setLocation("/logout"));
        logoutButton.getStyle().set("border-radius", "var(--lumo-border-radius-m)");

        HorizontalLayout rightSection = new HorizontalLayout(liveClockLabel, themeToggleButton, logoutButton);
        rightSection.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSection.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        rightSection.setSpacing(true);
        rightSection.setPadding(true);

        HorizontalLayout header = new HorizontalLayout(leftSection, rightSection);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle().set("background", "var(--vaadin-header-background, var(--lumo-base-color))");
        header.getStyle().set("border-bottom", "1px solid var(--vaadin-border-color, var(--lumo-divider-color))");
        header.getStyle().set("padding", "0 var(--lumo-space-m)");

        addToNavbar(header);

        UI.getCurrent().getPage().executeJs("""
            const label = document.getElementById('live-clock-label');
            if (label) {
                setInterval(() => {
                    const now = new Date();
                    label.textContent = now.toLocaleString('de-DE', {
                        year: 'numeric', month: '2-digit', day: '2-digit',
                        hour: '2-digit', minute: '2-digit', second: '2-digit'
                    });
                }, 1000);
            }
        """);
    }

    private Component createSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setPadding(true);
        sidebar.setSpacing(true);
        sidebar.setAlignItems(FlexComponent.Alignment.STRETCH);
        sidebar.getStyle().set("background", "var(--vaadin-header-background, var(--lumo-base-color))");

        sidebar.add(
                createSidebarLink("Home",    VaadinIcon.HOME,    MainView.class),
                createSidebarLink("Benutzer",   VaadinIcon.USER,    AdminView.class),
                createSidebarLink("Accounts", VaadinIcon.GROUP,  RoleView.class),
                createSidebarLink("Kassen",  VaadinIcon.CASH,    RegisterAddView.class),
                createSidebarLink("Bestand", VaadinIcon.PACKAGE, StockView.class),
                createSidebarLink("Belege",  VaadinIcon.RECORDS, DailyReceiptReportingView.class),
                createSidebarLink("Lasttests", VaadinIcon.CHART_LINE, PerformanceTestView.class)
        );
        return sidebar;
    }

    private RouterLink createSidebarLink(String text, VaadinIcon icon, Class<? extends Component> navigationTarget) {
        Icon i = new Icon(icon);
        i.getStyle().set("width", "24px");
        i.getStyle().set("height", "24px");

        Span textSpan = new Span(text);
        textSpan.getStyle().set("margin-left", "var(--lumo-space-m)");
        textSpan.getStyle().set("font-weight", "500");
        textSpan.getStyle().set("font-size", "var(--lumo-font-size-m)");

        RouterLink link = new RouterLink(navigationTarget);
        link.add(i, textSpan);
        link.getStyle().set("display", "flex");
        link.getStyle().set("align-items", "center");
        link.getStyle().set("padding", "var(--lumo-space-m)");
        link.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        link.getStyle().set("transition", "all 0.3s ease");
        link.getStyle().set("text-decoration", "none");
        link.getStyle().set("color", "var(--vaadin-text-color, var(--lumo-body-text-color))");
        link.getStyle().set("cursor", "pointer");

        // Hover-Effekt
        link.getElement().addEventListener("mouseenter", e -> {
            link.getStyle().set("background-color", "var(--lumo-primary-color-10pct)");
            link.getStyle().set("box-shadow", "0 2px 6px rgba(0, 0, 0, 0.1)");
        });
        link.getElement().addEventListener("mouseleave", e -> {
            link.getStyle().set("background-color", "transparent");
            link.getStyle().set("box-shadow", "none");
        });

        return link;

    }

    @PostConstruct
    public void initUI() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(FlexComponent.Alignment.STRETCH);

        receipts.addAll(receiptService.findAll());
        receiptGrid.setItems(receipts);

        initButtons();
        initFilters();
        configureGrid();

        content.add(getFiltersLayout(), receiptGrid);
        content.setFlexGrow(1, receiptGrid); // Grid soll den restlichen Platz einnehmen

        setContent(content);
    }

    private HorizontalLayout getFiltersLayout() {
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
        return filtersLayout;
    }

    private void initButtons() {
        generateDailyReceiptButton.addClickListener(c -> handleGenerateDailyReceiptButtonClick());
        debugSendReportingToLogistic.addClickListener(c -> dailyReceiptReportingSchedule.sendDailyReceiptReport());
    }

    private void initFilters() {
        // Filters are now created in getFiltersLayout() and added to content in initUI()
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

        UI.getCurrent().add(a); // Changed from add(a) to UI.getCurrent().add(a)
        a.getElement().callJsFunction("click");
        a.getElement().executeJs("this.remove()");
    }

    private void handleGenerateDailyReceiptButtonClick() {
        List<Receipt> tagesbelege = receiptService.findAll().stream()
                .filter(r -> r.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate().equals(LocalDate.now()))
                .filter(r -> r.getStore().equals(storeClient.getStore()))
                .toList();

        if (tagesbelege.isEmpty()) {
            Notification.show(
                    "Keine Belege für heute vorhanden. Tagesabschluss kann nicht erstellt werden.",
                    4000,
                    Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        try {
            ByteArrayInputStream generatedPdfStream = receiptService.generateDailyReceipt(tagesbelege);
            byte[] pdfBytes = generatedPdfStream.readAllBytes();

            String fileName = "Tagesabschluss-%d-%s.pdf".formatted(
                    storeClient.getStore().getId(), LocalDate.now()
            );

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

            UI.getCurrent().add(a);
            a.getElement().callJsFunction("click");
            a.getElement().executeJs("this.remove()");

            Notification.show(
                    "Tagesabschluss wird heruntergeladen.",
                    2500,
                    Notification.Position.TOP_CENTER
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            Notification.show(
                    "Fehler beim Erstellen des Tagesabschlusses: " + e.getMessage(),
                    5000,
                    Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
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

    private String getPageTitle() {
        PageTitle titleAnnotation = this.getClass().getAnnotation(PageTitle.class);
        return titleAnnotation != null ? titleAnnotation.value() : "Daily Receipt Reporting";
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Class<?> targetView = beforeEnterEvent.getNavigationTarget();

        RolesAllowed rolesAllowed = targetView.getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            beforeEnterEvent.rerouteTo("login");
            return;
        }

        Set<String> userAuthorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean authorized = Arrays.stream(rolesAllowed.value())
                .anyMatch(userAuthorities::contains);

        if (!authorized) {
            beforeEnterEvent.rerouteTo("login");
        }
    }
}
