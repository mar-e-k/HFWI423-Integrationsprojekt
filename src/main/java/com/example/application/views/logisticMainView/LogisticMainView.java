package com.example.application.views.logisticMainView;

import com.example.application.data.article.ArticleInfo;
import com.example.application.services.ArticleInfoService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.button.Button;
import com.example.application.services.StorageLocationService;
import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.views.stockChangeView.StockChangeDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;

/**
 * Hauptansicht "Logistik" mit Filterleiste und Grid für {@link ArticleInfo}.
 *
 * <p>Features:
 * - Responsive Filter (Desktop + einklappbar auf Mobile)
 * - Datenbindung via DataProvider
 * - "Neuen Artikel"-Dialog (lazy initialisiert)
 * </p>
 */
@PageTitle("Logistic Main View")                 // Titel im Browser-Tab
@Route("")                            // Root-Route
@Menu(order = 0, icon = LineAwesomeIconUrl.FILTER_SOLID)
@Uses(Icon.class)
public class LogisticMainView extends Div {

    private final StorageLocationService storageLocationService;
    private Grid<ArticleInfo> grid;
    private Filters filters;
    private final ArticleInfoService articleInfoService;

    public LogisticMainView(ArticleInfoService articleInfoService, StorageLocationService storageLocationService) {
        this.articleInfoService = articleInfoService;
        this.storageLocationService = storageLocationService;

        // === Grundlayout der Seite ===
        setSizeFull();
        addClassNames("gridwith-filters-view");

        // Filterleiste: ruft bei Änderungen/Buttons refreshGrid() auf
        filters = new Filters(this::refreshGrid);

        // Datengrid erzeugen (Spalten/Renderer/Selektor etc. im Helper kapseln)
        Component gridComponent = createGrid();
        setupDataProvider();

        // === Seite zusammensetzen ===
        // 1) Mobile Filter-Kopf (toggle), 2) volle Filterleiste (Desktop/ausklappbar mobil),
        // 3) Toolbar mit "Neuen Artikel", 4) Grid
        VerticalLayout layout = new VerticalLayout(
                createMobileFilters(),
                filters,
                gridComponent
        );
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);

        add(layout);

    }

    /**
     * Erstellt den mobilen Filter-Header (Kompaktzeile), mit dem die volle Filterleiste ein-/ausgeklappt wird.
     * Auf größeren Screens bleibt die Filterleiste typischerweise sichtbar; auf mobilen Geräten spart das Toggle Platz.
     */
    private HorizontalLayout createMobileFilters() {
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(
                LumoUtility.Padding.MEDIUM,
                LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER
        );
        mobileFilters.addClassName("mobile-filters");

        // Plus/Minus-Icon als visueller Zustand für eingeklappt/ausgeklappt
        Icon mobileIcon = new Icon("lumo", "plus");

        // Überschrift (übersetzbar halten; ggf. I18N verwenden)
        Span filtersHeading = new Span("Filter");
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading); // Text nimmt restliche Breite, Icon bleibt kompakt

        // Ein-/Ausklappen der Filterleiste
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });

        return mobileFilters;
    }

    /**
     * UI-Filterleiste für die Artikelsuche.
     * <p>
     * Stellt Eingabefelder für Artikelname, Artikelnummer, Mindestbestand
     * und Lagerort bereit sowie Aktionen zum Suchen und Zurücksetzen.
     * Diese Klasse implementiert außerdem {@link Specification} für ArticleInfo,
     * sodass dieselben Feldwerte für die Datenbank-Filterung (JPA Criteria) genutzt werden können.
     * <p>
     * - Platzhalter geben Beispielwerte an und reduzieren Fehleingaben.
     * - "Zurücksetzen" leert alle Felder und triggert sofort eine neue Suche.
     * - Artikelnummer nutzt ein IntegerField mit Step-Buttons und Min=0.
     */
    public static class Filters extends Div implements Specification<ArticleInfo> {

        // Eingabekomponenten (sichtbare Filterfelder)
        private final TextField articleName = new TextField("Article Name");       // Freitext, case-insensitive LIKE
        private final TextField articleNumber = new TextField("Article Number");  // Exakt gleich (=)
        private final TextField stockLevel = new TextField("Stock Level");           // Numerisch, >= Mindestbestand
        private final TextField storageLocation = new TextField("Storage Location");          // Freitext, case-insensitive LIKE

        /**
         * Erstellt die Filterleiste und verbindet die Buttons mit der onSearch Suchaktion.
         * <p>
         * onSearch Callback, der ausgeführt wird, wenn der Nutzer sucht oder zurücksetzt.
         * Lädt die Liste/Grids neu.
         */
        public Filters(Runnable onSearch) {
            // === Layout-Basis ===
            setWidthFull();
            addClassName("filter-layout");
            // Einheitliche Abstände & Box-Modell via Lumo Utility-Klassen
            addClassNames(
                    LumoUtility.Padding.Horizontal.LARGE,
                    LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER
            );

            // === Feld-Konfiguration (Platzhalter & Validierung) ===
            articleName.setPlaceholder("Search Name");

            articleNumber.setPlaceholder("Search Number");
            stockLevel.setPlaceholder("Minimum Inventory"); // Wird später als Integer geparst (mit Fallback)

            storageLocation.setPlaceholder("Search Storage Location");

            // === Aktionen ===

            // Leert alle Felder. Aktualisiert sofort
            // Anwender sieht Ungefiltertes Ergebnis
            Button resetBtn = new Button("Reset Search", e -> {
                articleName.clear();
                articleNumber.clear();
                stockLevel.clear();
                storageLocation.clear();
                onSearch.run();
            });
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY); // Sekundäre/tertiäre Gewichtung im UI

            // Startet die Suche mit den aktuell eingegebenen Filterwerten.
            Button searchBtn = new Button("Search Article", e -> onSearch.run());
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY); // Primäre Aktion im UI

            // Buttons gruppieren (Abstand festlegen)
            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            // Komponenten der Ansicht hinzufügen (Reihenfolge = angezeigte Reihenfolge auf der UI)
            add(articleName, articleNumber, stockLevel, storageLocation, actions);
        }

        @Override
        /**
         * Baut dynamisch ein WHERE-Predicate für Artikel anhand optionaler Filterfelder.
         * Verknüpft alle gefundenen Bedingungen mit AND. Wenn kein Filter gesetzt ist,
         * wird ein "immer wahr" (cb.conjunction()) zurückgegeben, sodass keine Einschränkung erfolgt.
         *
         * Verwendete Filter:
         * - articleName (LIKE, case-insensitive)
         * - articleNumber (exakte Übereinstimmung)
         * - inventory (numerisch: >=)
         * - storageLocation (LIKE, case-insensitive)
         */
        public Predicate toPredicate(Root<ArticleInfo> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            // Liste sammelt alle optionalen Filterbedingungen
            List<Predicate> ps = new ArrayList<>();

            // Textsuche auf dem Artikelnamen (case-insensitive, enthält)
            if (!articleName.isEmpty()) {
                // Suchmuster: %eingabe%
                String v = "%" + articleName.getValue().toLowerCase() + "%";
                // LOWER(dbSpalte) LIKE lower(eingabe)
                ps.add(cb.like(cb.lower(root.get("name")), v));
            }

            // Exakte Übereinstimmung der Artikelnummer (Integer)
            if (!articleNumber.isEmpty()) {
                ps.add(cb.equal(root.get("articleNumber"), articleNumber.getValue().trim()));
            }

            // Mindestbestand: inventory >= eingegebener Wert
            if (!stockLevel.isEmpty()) {
                try {
                    int inv = Integer.parseInt(stockLevel.getValue().trim());
                    ps.add(cb.greaterThanOrEqualTo(root.get("stockLevel"), inv));
                } catch (NumberFormatException ignored) {
                    // Ungültige Zahl => Filter wird einfach nicht angewandt (kein Fehlerwurf)
                }
            }

            // Textsuche auf Lagerort (case-insensitive, enthält)
            if (!storageLocation.isEmpty()) {
                String v = "%" + storageLocation.getValue().toLowerCase() + "%";
                ps.add(cb.like(cb.lower(root.get("storageLocation")), v));
            }

            // Wenn keine Bedingungen vorhanden sind => "immer wahr" zurückgeben (keine Filterung)
            // Sonst alle Bedingungen mit AND verknüpfen
            return ps.isEmpty() ? cb.conjunction() : cb.and(ps.toArray(Predicate[]::new));
        }
    }


    /**
     * Erstellt das Grid zur Anzeige von {@link ArticleInfo}.
     * Definiert Spalten, Header, Sortierung und Layout.
     *
     * @return das konfigurierte Grid als Component
     */
    private Component createGrid() {

        grid = new Grid<>(ArticleInfo.class, false);

        // Spalte: Artikelname (Text)
        grid.addColumn(ArticleInfo::getName)
                .setHeader("Article Name")
                .setKey("articleName")     // Key für spätere Referenzen/Tests
                .setAutoWidth(true)        // passt sich Inhalt an, verhindert horizontales Scrollen
                .setSortable(true);

        // Spalte: Artikelnummer (Integer)
        grid.addColumn(ArticleInfo::getArticleNumber)
                .setHeader("Article Number")
                .setKey("articleNumber")
                .setAutoWidth(true)
                .setSortable(true);

        // Spalte: Bestand (Integer)
        grid.addColumn(ArticleInfo::getStockLevel)
                .setHeader("Stock Level")
                .setKey("stockLevel")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addComponentColumn(item -> {
            Button editStock = new Button("Edit stock");
            editStock.addClickListener(e -> {
                StockChangeDialog dlg = new StockChangeDialog(
                        articleInfoService,
                        item,
                        this::refreshGrid   // damit das Grid danach aktualisiert wird
                );
                dlg.open();
            });
            return editStock;
        }).setHeader("Actions");

        // Spalte: Lagerort (Text)
        grid.addComponentColumn(item -> {
                    String label = item.getStorageLocation() != null && !item.getStorageLocation().isBlank()
                            ? item.getStorageLocation()
                            : "Select location";

                    Button link = new Button(label);
                    link.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
                    link.addClickListener(e -> openAvailableLocationsDialog(item));

                    return link;
                }).setHeader("Storage Location")
                .setKey("storageLocation")
                .setAutoWidth(true)
                .setSortable(false);

        grid.addColumn(ArticleInfo::getReserveStorageLocation)
                .setHeader("Reserve Storage Location")
                .setKey("reserveStorageLocation")
                .setAutoWidth(true)
                .setSortable(true);

        //verschiedenfarbige Streifen + Umbruch langer Inhalte
        grid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_WRAP_CELL_CONTENT
        );

        // Grid füllt verfügbaren Platz unter Div und Search feldern
        grid.setSizeFull();

        return grid;
    }

    private String buildGeneralId(com.example.application.data.storageLocation.StorageLocation s) {
        if (s == null) {
            return "";
        }

        String zone = s.getStorageZone();           // z.B. "Zone 3"
        String zoneNumber = "";
        if (zone != null) {
            zoneNumber = zone.replace("Zone", "").trim(); // -> "3"
        }

        Integer shelf = s.getShelfID();
        Integer compartment = s.getCompartmentID();

        return "Z" + zoneNumber
                + ".S" + (shelf != null ? shelf : 0)
                + ".C" + (compartment != null ? compartment : 0);
    }

    private void openAvailableLocationsDialog(ArticleInfo article) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Available locations for: " + article.getName());

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);

        // Filter: Zone
        ComboBox<String> zoneFilter = new ComboBox<>("Zone");
        zoneFilter.setItems("All zones", "Zone 1", "Zone 2", "Zone 3", "Zone 4");
        zoneFilter.setValue("All zones");

        Grid<StorageLocation> locGrid = new Grid<>(StorageLocation.class, false);
        locGrid.addColumn(StorageLocation::getStorageZone).setHeader("Zone").setAutoWidth(true);
        locGrid.addColumn(StorageLocation::getShelfID).setHeader("Shelf").setAutoWidth(true);
        locGrid.addColumn(StorageLocation::getCompartmentID).setHeader("Compartment").setAutoWidth(true);
        locGrid.addColumn(StorageLocation::getStorageStatus).setHeader("Status").setAutoWidth(true);
        locGrid.addColumn(StorageLocation::getGeneralId).setHeader("General ID").setAutoWidth(true);

        List<StorageLocation> allAvailable = storageLocationService.findAllAvailable();
        locGrid.setItems(allAvailable);
        locGrid.setSizeFull();

        zoneFilter.addValueChangeListener(e -> {
            String value = e.getValue();
            if (value == null || "All zones".equals(value)) {
                locGrid.setItems(allAvailable);
            } else {
                locGrid.setItems(
                        allAvailable.stream()
                                .filter(loc -> value.equals(loc.getStorageZone()))
                                .toList()
                );
            }
        });

        // Klick → Bestätigung + Zuweisung
        locGrid.addItemClickListener(event -> {
            StorageLocation selected = event.getItem();
            String generalId = selected.getGeneralId();

            ConfirmDialog confirm = new ConfirmDialog();
            confirm.setHeader("Assign location");
            String existingLoc = article.getStorageLocation();
            if (existingLoc != null && !existingLoc.isBlank()) {
                confirm.setText("Article already has location " + existingLoc
                        + ". Do you want to move it to " + generalId + "?");
            } else {
                confirm.setText("Assign location " + generalId + " to article " + article.getName() + "?");
            }

            confirm.setCancelable(true);
            confirm.setConfirmText("Assign");
            confirm.addConfirmListener(ev -> {
                try {
                    // 1) Alte Location zurück auf Available
                    String oldGeneralId = article.getStorageLocation();
                    if (oldGeneralId != null && !oldGeneralId.isBlank()) {
                        StorageLocation parsed = parseGeneralIdToLocation(oldGeneralId);
                        if (parsed != null) {
                            storageLocationService
                                    .findByZoneShelfCompartment(
                                            parsed.getStorageZone(),
                                            parsed.getShelfID(),
                                            parsed.getCompartmentID()
                                    )
                                    .ifPresent(oldLoc -> {
                                        oldLoc.setStorageStatus("Available");
                                        storageLocationService.save(oldLoc);
                                    });
                        }
                    }

                    // 2) Artikel-Location updaten (Backend + UI)
                    articleInfoService.updateStorageLocation(article.getId(), generalId);
                    article.setStorageLocation(generalId);

                    // 3) neuen Lagerplatz auf Used
                    selected.setStorageStatus("Used");
                    storageLocationService.save(selected);

                    // 4) Zeile im Article-Grid aktualisieren
                    grid.getDataProvider().refreshItem(article);

                    Notification.show("Location assigned: " + generalId);
                    dialog.close();
                } catch (Exception ex) {
                    Notification.show("Could not assign location");
                    ex.printStackTrace();
                }
            });

            confirm.open();
        });

        layout.add(zoneFilter, locGrid);
        layout.setFlexGrow(1, locGrid);
        dialog.add(layout);

        Button close = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(close);

        dialog.setWidth("900px");
        dialog.setHeight("500px");

        dialog.open();
    }

    private void setupDataProvider() {
        DataProvider<ArticleInfo, Void> dataProvider = DataProvider.fromCallbacks(
                (Query<ArticleInfo, Void> q) -> articleInfoService
                        .list(VaadinSpringDataHelpers.toSpringPageRequest(q), filters)
                        .stream(),
                (Query<ArticleInfo, Void> q) -> (int) articleInfoService.count(filters)
        );

        grid.setDataProvider(dataProvider);
    }

    private com.example.application.data.storageLocation.StorageLocation parseGeneralIdToLocation(String generalId) {
        if (generalId == null || generalId.isBlank()) {
            return null;
        }

        // Erwartetes Format: Z3.S2.C4
        try {
            String[] parts = generalId.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String zonePart = parts[0]; // "Z3"
            String shelfPart = parts[1]; // "S2"
            String compPart = parts[2]; // "C4"

            int zoneNumber = Integer.parseInt(zonePart.substring(1));
            int shelfId = Integer.parseInt(shelfPart.substring(1));
            int compId = Integer.parseInt(compPart.substring(1));

            String zoneString = "Zone " + zoneNumber;

            // Nur als Transport-Objekt verwenden, um die drei Werte zu halten
            StorageLocation tmp = new StorageLocation();
            tmp.setStorageZone(zoneString);
            tmp.setShelfID(shelfId);
            tmp.setCompartmentID(compId);
            return tmp;
        } catch (Exception e) {
            return null;
        }
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}