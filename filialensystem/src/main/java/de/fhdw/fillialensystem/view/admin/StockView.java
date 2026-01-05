package de.fhdw.fillialensystem.view.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.Lumo;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.fillialensystem.persistence.entity.StoreLinkStock;
import de.fhdw.fillialensystem.persistence.service.StoreLinkStockService;
import de.fhdw.fillialensystem.view.MainView;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;
import java.util.stream.Collectors;

@Route("/admin-stock")
@PageTitle("Bestandsübersicht")
@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
public class StockView extends AppLayout implements BeforeEnterObserver {

    private final StoreLinkStockService storeLinkStockService;

    private Grid<StoreLinkStock> grid;
    private TextField searchField;

    private List<StoreLinkStock> allStocks;

    public StockView(StoreLinkStockService storeLinkStockService) {
        this.storeLinkStockService = storeLinkStockService;
        createHeader();
        addToDrawer(createSidebar());
    }

    private void createHeader() {
        H1 viewTitle = new H1(getPageTitle());
        HorizontalLayout leftSection = new HorizontalLayout(new DrawerToggle(), viewTitle);
        leftSection.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Span liveClockLabel = new Span();
        liveClockLabel.setId("live-clock-label");
        liveClockLabel.getStyle().set("font-size", "var(--lumo-font-size-l)");
        liveClockLabel.getStyle().set("font-weight", "bold");

        Button themeToggleButton = new Button(new Icon(VaadinIcon.ADJUST), click -> {
            UI.getCurrent().getPage().executeJs("return document.documentElement.getAttribute('theme');")
                    .then(String.class, currentClientTheme -> {
                        var themeList = UI.getCurrent().getElement().getThemeList();
                        boolean isClientDark = "dark".equals(currentClientTheme);

                        if (isClientDark) {
                            themeList.remove(Lumo.DARK);
                            UI.getCurrent().getPage().executeJs("localStorage.setItem('theme', 'light');");
                            UI.getCurrent().getPage().executeJs("document.documentElement.removeAttribute('theme');");
                        } else {
                            themeList.add(Lumo.DARK);
                            UI.getCurrent().getPage().executeJs("localStorage.setItem('theme', 'dark');");
                            UI.getCurrent().getPage().executeJs("document.documentElement.setAttribute('theme', 'dark');");
                        }
                    });
        });
        themeToggleButton.setTooltipText("Toggle dark mode");

        Button logoutButton = new Button("Logout", e -> UI.getCurrent().getPage().setLocation("/logout"));

        HorizontalLayout rightSection = new HorizontalLayout(liveClockLabel, themeToggleButton, logoutButton);
        rightSection.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSection.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        rightSection.setSpacing(true);

        HorizontalLayout header = new HorizontalLayout(leftSection, rightSection);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

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
        sidebar.setPadding(false);
        sidebar.setSpacing(false);
        sidebar.setAlignItems(FlexComponent.Alignment.STRETCH);

        sidebar.add(
                createSidebarLink("Home", VaadinIcon.HOME, MainView.class),
                createSidebarLink("Admin View", VaadinIcon.USER, AdminView.class),
                createSidebarLink("Role View", VaadinIcon.GROUP, RoleView.class),
                createSidebarLink("Register View", VaadinIcon.CASH, RegisterAddView.class),
                createSidebarLink("Stock View", VaadinIcon.PACKAGE, StockView.class),
                createSidebarLink("Daily Receipt Reporting", VaadinIcon.RECORDS, DailyReceiptReportingView.class)
        );
        return sidebar;
    }

    private RouterLink createSidebarLink(String text, VaadinIcon icon, Class<? extends Component> navigationTarget) {
        com.vaadin.flow.component.icon.Icon i = new com.vaadin.flow.component.icon.Icon(icon);
        Span textSpan = new Span(text);
        textSpan.getStyle().set("margin-left", "var(--lumo-space-m)");

        RouterLink link = new RouterLink(navigationTarget);
        link.add(i, textSpan);
        link.getStyle().set("display", "flex");
        link.getStyle().set("align-items", "center");
        link.getStyle().set("padding", "var(--lumo-space-s)");
        link.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        link.getStyle().set("transition", "background-color 0.2s");
        link.getStyle().set("text-decoration", "none");
        link.getStyle().set("color", "var(--lumo-body-text-color)");

        return link;
    }

    @PostConstruct
    public void initUI() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(FlexComponent.Alignment.STRETCH);

        // Suchfeld
        searchField = new TextField("Artikel suchen");
        searchField.setPlaceholder("Name oder Artikelnummer");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("300px");
        searchField.addValueChangeListener(e -> applyFilter());

        grid = createGrid();

        loadStocks();      // lädt + sortiert allStocks
        applyFilter();     // wendet Filter an und setzt Items im Grid

        HorizontalLayout searchLayout = new HorizontalLayout(searchField);
        searchLayout.setWidthFull();
        searchLayout.setAlignItems(FlexComponent.Alignment.END);

        content.add(searchLayout, grid);
        content.setFlexGrow(1, grid);

        setContent(content);
    }

    private Grid<StoreLinkStock> createGrid() {
        Grid<StoreLinkStock> grid = new Grid<>(StoreLinkStock.class, false);
        grid.setSizeFull();

        // Filiale – über ID (oder Name, falls vorhanden)
        grid.addColumn(stock -> stock.getStore().getId())
                .setHeader("Filiale-ID")
                .setAutoWidth(true);

        // Artikelname
        grid.addColumn(stock -> stock.getArticle().getName())
                .setHeader("Artikel")
                .setAutoWidth(true);

        // Artikelnummer
        grid.addColumn(stock -> stock.getArticle().getArticleNumber())
                .setHeader("Artikelnummer")
                .setAutoWidth(true);

        // Mindestbestand – Anzeige + "Ändern"-Button, Dialog übernimmt Speichern
        grid.addComponentColumn(this::createMinStockCell)
                .setHeader("Mindestbestand")
                .setAutoWidth(true);

        // Bestand mit Markierung, wenn amount < minBestand
        grid.addComponentColumn(this::createAmountCell)
                .setHeader("Bestand")
                .setAutoWidth(true)
                .setComparator(this::stockDiff); // Sortierlogik: am stärksten unter Min zuerst

        // Aktiv / inaktiv
        grid.addColumn(StoreLinkStock::isActive)
                .setHeader("Aktiv")
                .setAutoWidth(true);

        return grid;
    }

    /**
     * Zelle für Mindestbestand:
     * - zeigt aktuellen Wert
     * - Button "Ändern" öffnet Dialog mit Eingabefeld + Speichern/Abbrechen
     */
    private Component createMinStockCell(StoreLinkStock stock) {
        int currentMin = Objects.requireNonNullElse(stock.getMinimumStockLevel(), 5);
        Span valueLabel = new Span(String.valueOf(currentMin));

        Button edit = new Button("Ändern", e -> openMinStockDialog(stock));

        HorizontalLayout layout = new HorizontalLayout(valueLabel, edit);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        return layout;
    }

    /**
     * Dialog zum Ändern des Mindestbestands:
     * - Speichern/Abbrechen
     * - nach Speichern: DB-Update, neu laden, Markierungen + Sortierung aktualisiert
     */
    private void openMinStockDialog(StoreLinkStock stock) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Mindestbestand ändern");

        H3 title = new H3("Artikel: " + stock.getArticle().getName());
        title.getStyle().set("margin-top", "0");

        IntegerField field = new IntegerField("Mindestbestand");
        field.setMin(0);
        field.setWidth("150px");
        field.setValue(Objects.requireNonNullElse(stock.getMinimumStockLevel(), 5));

        VerticalLayout layout = new VerticalLayout(title, field);
        layout.setPadding(false);
        layout.setSpacing(true);
        dialog.add(layout);

        Button cancel = new Button("Abbrechen", e -> dialog.close());

        Button save = new Button("Speichern", e -> {
            Integer value = field.getValue();
            if (value == null || value < 0) {
                Notification.show("Mindestbestand muss >= 0 sein.",
                                3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            stock.setMinimumStockLevel(value);
            storeLinkStockService.update(stock);

            // neu laden & sortieren -> Markierung + Reihenfolge aktualisieren
            loadStocks();
            applyFilter();

            Notification.show("Mindestbestand aktualisiert.",
                            2000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            dialog.close();
        });

        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    /**
     * Markierung:
     * amount < minStockLevel ⟶ orange + Tooltip "Niedriger Bestand"
     */
    private Component createAmountCell(StoreLinkStock stock) {
        int amount = stock.getAmount();
        int min = Objects.requireNonNullElse(stock.getMinimumStockLevel(), 5);

        Span span = new Span(amount + " Stück");

        if (amount < min) {
            span.getStyle().set("background-color", "orange");
            span.getStyle().set("color", "black");
            span.getStyle().set("padding", "2px 6px");
            span.getStyle().set("border-radius", "4px");
            span.getElement().setProperty("title", "Niedriger Bestand");
        }

        return span;
    }

    /**
     * Differenz zwischen Bestand und Mindestbestand.
     * < 0  -> unter Mindestbestand (soll nach oben)
     * == 0 -> genau auf Min
     * > 0  -> darüber
     */
    private int stockDiff(StoreLinkStock s) {
        int min = Objects.requireNonNullElse(s.getMinimumStockLevel(), 5);
        return s.getAmount() - min;
    }

    /**
     * Lädt alle Bestände und sortiert sie:
     * - stärkster Mangel (größtes negatives diff) zuerst
     */
    private void loadStocks() {
        allStocks = storeLinkStockService.findAll();
        allStocks.sort(Comparator.comparingInt(this::stockDiff));
    }

    /**
     * Filtert allStocks anhand des Suchfeldes (Name / Artikelnummer)
     * und setzt die Items im Grid.
     */
    private void applyFilter() {
        if (allStocks == null) {
            return;
        }

        String filterText = searchField != null ? searchField.getValue() : "";
        String filter = filterText == null ? "" : filterText.trim().toLowerCase(Locale.ROOT);

        List<StoreLinkStock> filtered = allStocks.stream()
                .filter(stock -> {
                    if (filter.isEmpty()) {
                        return true;
                    }
                    String name = Objects.toString(stock.getArticle().getName(), "").toLowerCase(Locale.ROOT);
                    String number = Objects.toString(stock.getArticle().getArticleNumber(), "").toLowerCase(Locale.ROOT);
                    return name.contains(filter) || number.contains(filter);
                })
                .collect(Collectors.toList());

        grid.setItems(filtered);
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

    private String getPageTitle() {
        PageTitle titleAnnotation = this.getClass().getAnnotation(PageTitle.class);
        return titleAnnotation != null ? titleAnnotation.value() : "Bestandsübersicht";
    }
}
