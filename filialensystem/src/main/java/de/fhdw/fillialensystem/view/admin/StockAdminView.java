package de.fhdw.fillialensystem.view.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.commons.view.AbstractMainView;
import de.fhdw.fillialensystem.persistence.entity.StoreLinkStock;
import de.fhdw.fillialensystem.persistence.service.StoreLinkStockService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Route("/admin-stock")
@PageTitle("Bestandsübersicht")
@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
public class StockAdminView extends AbstractMainView {

    private final StoreLinkStockService storeLinkStockService;

    private Grid<StoreLinkStock> grid;
    private TextField searchField;

    private List<StoreLinkStock> allStocks;

    public StockAdminView(StoreLinkStockService storeLinkStockService) {
        this.storeLinkStockService = storeLinkStockService;
    }

    @Override
    protected HorizontalLayout createTopBarButtons() {
        Button backToAdminBtn = new Button("Zum Admin-Dashboard");
        backToAdminBtn.addClickListener(e -> UI.getCurrent().navigate("admin"));
        return new HorizontalLayout(backToAdminBtn);
    }

    @Override
    protected void init() {
    }

    @PostConstruct
    public void initUI() {
        setSizeFull();
        setAlignItems(Alignment.STRETCH);
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Bestände je Filiale / Artikel");

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
        searchLayout.setAlignItems(Alignment.END);

        VerticalLayout content = new VerticalLayout(title, searchLayout, grid);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(true);
        content.setAlignItems(Alignment.STRETCH);

        add(content);
        setFlexGrow(1, content);
        setFlexGrow(1, grid);
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
        int currentMin = Objects.requireNonNullElse(stock.getMinStockLevel(), 5);
        Span valueLabel = new Span(String.valueOf(currentMin));

        Button edit = new Button("Ändern", e -> openMinStockDialog(stock));

        HorizontalLayout layout = new HorizontalLayout(valueLabel, edit);
        layout.setAlignItems(Alignment.CENTER);
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
        field.setValue(Objects.requireNonNullElse(stock.getMinStockLevel(), 5));

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

            stock.setMinStockLevel(value);
            storeLinkStockService.update(stock.getId(), stock);

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
        int min = Objects.requireNonNullElse(stock.getMinStockLevel(), 5);

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
        int min = Objects.requireNonNullElse(s.getMinStockLevel(), 5);
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
}
