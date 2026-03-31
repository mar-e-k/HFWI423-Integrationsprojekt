package com.example.application.views.logisticMainView;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.services.ArticleInfoService;
import com.example.application.views.components.StorageLocationPickerDialog;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.button.Button;
import com.example.application.services.StorageLocationService;
import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.views.components.StockChangeDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
 * Features:
 * - Responsive Filter (Desktop + einklappbar auf Mobile)
 * - Datenbindung via DataProvider
 * - "Neuen Artikel"-Dialog (lazy initialisiert)
 */
@PageTitle("Artikelübersicht")                 // Titel im Browser-Tab
@Route("")                                          // Root-Route (Startseite der App)
@Menu(order = 0, icon = LineAwesomeIconUrl.FILTER_SOLID)
@Uses(Icon.class)
public class LogisticMainView extends Div {

    // Service für Artikel-spezifische Datenbankzugriffe
    private final ArticleInfoService articleInfoService;

    // Service für Lagerplatz-spezifische Datenbankzugriffe
    private final StorageLocationService storageLocationService;

    // Grid, in dem die Artikel angezeigt werden
    private Grid<ArticleInfo> grid;

    // Filterkomponente, mit der der Benutzer die Artikelliste einschränken kann
    private Filters filters;

    public LogisticMainView(ArticleInfoService articleInfoService, StorageLocationService storageLocationService) {
        this.articleInfoService = articleInfoService;
        this.storageLocationService = storageLocationService;

        // === Grundlayout der Seite ===
        setSizeFull();
        addClassNames("gridwith-filters-view", "logistic-main-view");

        // Filterleiste: ruft bei Änderungen/Buttons refreshGrid() auf
        filters = new Filters(this::refreshGrid);

        // Datengrid erzeugen (Spalten/Renderer/Selektor etc. im Helper kapseln)
        Component gridComponent = createGrid();
        setupDataProvider(); // Datenquelle an das Grid binden

        VerticalLayout layout = new VerticalLayout(
                createMobileFilters(),
                filters,
                gridComponent
        );
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);

        Div card = new Div(layout);
        card.addClassName("content-card");
        card.setSizeFull();

        add(card);
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
        mobileFilters.addClassName("mobile-filters"); // für eigenes CSS

        // Plus/Minus-Icon als visueller Zustand für eingeklappt/ausgeklappt
        Icon mobileIcon = new Icon("lumo", "plus");

        // Überschrift (könnte später über I18N übersetzt werden)
        Span filtersHeading = new Span("Filter");
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading); // Text nimmt restliche Breite, Icon bleibt kompakt

        // Ein-/Ausklappen der Filterleiste bei Klick auf die Kopfzeile
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                // Filter sind sichtbar → ausblenden
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                // Filter sind versteckt → einblenden
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
     * - Artikelnummer und Bestand werden als String eingegeben und später im Predicate behandelt.
     */
    public static class Filters extends Div implements Specification<ArticleInfo> {

        // Eingabekomponenten (sichtbare Filterfelder)
        private final TextField articleName = new TextField("Article Name");       // Freitext, case-insensitive LIKE
        private final TextField articleNumber = new TextField("Article Number");   // Exakt gleich (=), aber String
        private final TextField stockLevel = new TextField("Stock Level");         // Numerisch, >= Mindestbestand
        private final TextField storageLocation = new TextField("Storage Location"); // Freitext, case-insensitive LIKE

        /**
         * Erstellt die Filterleiste und verbindet die Buttons mit der onSearch Suchaktion.
         * <p>
         * onSearch: Callback, der ausgeführt wird, wenn der Nutzer sucht oder zurücksetzt.
         * Lädt die Liste/ das Grid neu.
         */
        public Filters(Runnable onSearch) {
            setWidthFull();
            addClassName("filter-layout");

            articleName.setPlaceholder("Search name");
            articleNumber.setPlaceholder("Search number");
            stockLevel.setPlaceholder("Minimum inventory");
            storageLocation.setPlaceholder("Search storage location");

            articleName.addClassName("filter-field");
            articleNumber.addClassName("filter-field");
            stockLevel.addClassName("filter-field");
            storageLocation.addClassName("filter-field");

            Button resetBtn = new Button("Reset", e -> {
                articleName.clear();
                articleNumber.clear();
                stockLevel.clear();
                storageLocation.clear();
                onSearch.run();
            });
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button searchBtn = new Button("Search", e -> onSearch.run());
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName("filter-actions");

            add(articleName, articleNumber, stockLevel, storageLocation, actions);
        }

        /**
         * Baut dynamisch ein WHERE-Predicate für Artikel anhand optionaler Filterfelder.
         * Verknüpft alle gefundenen Bedingungen mit AND. Wenn kein Filter gesetzt ist,
         * wird ein "immer wahr" (cb.conjunction()) zurückgegeben, sodass keine Einschränkung erfolgt.
         *
         * Verwendete Filter:
         * - articleName (LIKE, case-insensitive)
         * - articleNumber (exakte Übereinstimmung)
         * - inventory / stockLevel (numerisch: >=)
         * - storageLocation (LIKE, case-insensitive)
         */
        @Override
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

            // Exakte Übereinstimmung der Artikelnummer (hier als String)
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
        grid.addClassName("article-grid");

        grid.addColumn(ArticleInfo::getName)
                .setHeader("Artikelname")
                .setKey("articleName")
                .setFlexGrow(2)
                .setSortable(true);

        grid.addColumn(ArticleInfo::getArticleNumber)
                .setHeader("Artikelnummer")
                .setKey("articleNumber")
                .setFlexGrow(1)
                .setSortable(true);

        grid.addComponentColumn(item -> {
            Span s = new Span(String.valueOf(item.getTotalStock()));
            s.getStyle().set("font-weight", "700").set("color", "#1e293b").set("font-size", "0.9rem");
            return s;
        }).setHeader("Total Stock").setAutoWidth(true).setSortable(false);

        grid.addComponentColumn(item -> {
            int stock = item.getStockLevel() != null ? item.getStockLevel() : 0;
            int min   = item.getMinStock()   != null ? item.getMinStock()   : 0;
            Span badge = new Span(String.valueOf(stock));
            badge.getStyle()
                .set("padding", "2px 10px")
                .set("border-radius", "999px")
                .set("font-weight", "600")
                .set("font-size", "0.8rem");
            if (stock == 0) {
                badge.getStyle().set("background", "#fee2e2").set("color", "#dc2626");
            } else if (min > 0 && stock < min) {
                badge.getStyle().set("background", "#fef3c7").set("color", "#d97706");
            } else {
                badge.getStyle().set("background", "#dcfce7").set("color", "#16a34a");
            }
            return badge;
        }).setHeader("Pick Stock").setAutoWidth(true).setSortable(false);

        grid.addComponentColumn(item -> {
            int pal = item.getReservePallets() != null ? item.getReservePallets() : 0;
            Span s = new Span(pal + " Pal.");
            s.getStyle().set("color", "#64748b").set("font-size", "0.85rem").set("font-weight", "500");
            return s;
        }).setHeader("Reserve").setAutoWidth(true).setSortable(false);

        grid.addComponentColumn(item -> {
            Button editStock = new Button("Bestand");
            editStock.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            editStock.addClassName("edit-stock-btn");
            editStock.addClickListener(e -> new StockChangeDialog(articleInfoService, item, this::refreshGrid).open());
            return editStock;
        }).setHeader("Aktion").setAutoWidth(true).setFlexGrow(0);

        grid.addComponentColumn(item -> {
            boolean hasLocation = item.getStorageLocation() != null && !item.getStorageLocation().isBlank();
            String label = hasLocation ? item.getStorageLocation() : "+ Lagerplatz";

            Span badge = new Span(label);
            badge.getStyle()
                .set("padding", "3px 10px")
                .set("border-radius", "999px")
                .set("font-size", "0.8rem")
                .set("font-weight", "600")
                .set("cursor", "pointer")
                .set("transition", "opacity 0.15s");
            if (hasLocation) {
                badge.getStyle()
                    .set("background", "#dbeafe")
                    .set("color", "#1d4ed8");
            } else {
                badge.getStyle()
                    .set("background", "#f1f5f9")
                    .set("color", "#94a3b8")
                    .set("border", "1px dashed #cbd5e1");
            }
            badge.addClickListener(e -> openAvailableLocationsDialog(item));
            return badge;
        }).setHeader("Lagerplatz")
          .setKey("storageLocation")
          .setAutoWidth(true)
          .setFlexGrow(0);

        grid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_WRAP_CELL_CONTENT,
                GridVariant.LUMO_NO_BORDER
        );

        grid.setSizeFull();
        return grid;
    }

    /**
     * Baut aus einem StorageLocation-Objekt eine kompakte ID wie "Z3.S2.C4".
     * Wird zum Anzeigen bzw. Speichern des Lagerorts benutzt.
     */
    private String buildGeneralId(com.example.application.data.storageLocation.StorageLocation s) {
        if (s == null) {
            return "";
        }

        String zone = s.getStorageZone();           // z.B. "Zone 3"
        String zoneNumber = "";
        if (zone != null) {
            // "Zone 3" → "3"
            zoneNumber = zone.replace("Zone", "").trim();
        }

        Integer shelf = s.getShelfID();
        Integer compartment = s.getCompartmentID();

        // Fallback auf 0, falls shelf/compartment null sind
        return "Z" + zoneNumber
                + ".S" + (shelf != null ? shelf : 0)
                + ".C" + (compartment != null ? compartment : 0);
    }

    /**
     * Öffnet den Dialog mit allen verfügbaren Lagerplätzen
     * und weist bei Bestätigung dem Artikel die ausgewählte Location zu.
     */
    private void openAvailableLocationsDialog(ArticleInfo article) {

        StorageLocationPickerDialog picker = new StorageLocationPickerDialog(
                storageLocationService,
                "Available locations for: " + article.getName(),
                selected -> {

                    // wenn nichts ausgewählt wurde, einfach abbrechen
                    if (selected == null) {
                        return;
                    }

                    String generalId = selected.getGeneralId();

                    // Sicherheits-Confirm, damit der User nicht versehentlich verschiebt
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
                            // Alte Location wieder auf "Available" setzen
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

                            // Artikel-Location in der DB updaten
                            articleInfoService.updateStorageLocation(article.getId(), generalId);
                            article.setStorageLocation(generalId); // auch das Objekt im Grid aktualisieren

                            // neue Location auf "Used" setzen
                            selected.setStorageStatus("Used");
                            storageLocationService.save(selected);

                            // Grid-Zeile aktualisieren (nur diesen Artikel)
                            grid.getDataProvider().refreshItem(article);

                            // Erfolgs-Nachricht anzeigen
                            Notification n = Notification.show("Location assigned: " + generalId);
                            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                        } catch (Exception ex) {
                            // Fehlerfall: Meldung + Stacktrace im Log
                            Notification n = Notification.show("Could not assign location", 4000, Notification.Position.MIDDLE);
                            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                            ex.printStackTrace();
                        }
                    });

                    confirm.open();
                });

        picker.open(); // Dialog anzeigen
    }

    /**
     * Richtet den DataProvider für das Grid ein.
     * Verwendet Callbacks, damit Paging + Filter über den Service abgewickelt werden.
     */
    private void setupDataProvider() {
        DataProvider<ArticleInfo, Void> dataProvider = DataProvider.fromCallbacks(
                // Callback für das Laden einer Seite
                (Query<ArticleInfo, Void> q) -> articleInfoService
                        .list(VaadinSpringDataHelpers.toSpringPageRequest(q), filters)
                        .stream(),
                // Callback für das Zählen aller passenden Einträge
                (Query<ArticleInfo, Void> q) -> (int) articleInfoService.count(filters)
        );

        grid.setDataProvider(dataProvider);
    }

    /**
     * Parsed eine generalId wie "Z3.S2.C4" zurück in ein StorageLocation-Objekt
     * (nur als Container für Zone/Shelf/Compartment, nicht direkt aus der DB).
     */
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
            // Falls Parsing schiefgeht, einfach null zurückgeben
            return null;
        }
    }

    /**
     * Aktualisiert alle Daten im Grid (z.B. nach Filteränderung oder nach Dialog).
     */
    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}