package com.example.application.views.gridwithfilters;

import com.example.application.data.ArticleInfo;
import com.example.application.services.ArticleInfoService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;


/**
 * Hauptansicht "Logistik" mit Filterleiste und Grid für {@link ArticleInfo}.
 *
 * <p>Features:
 * - Responsive Filter (Desktop + einklappbar auf Mobile)
 * - Datenbindung via DataProvider
 * - "Neuen Artikel"-Dialog (lazy initialisiert)
 * </p>
 */
@PageTitle("Logistic")                 // Titel im Browser-Tab
@Route("")                            // Root-Route
@Menu(order = 0, icon = LineAwesomeIconUrl.FILTER_SOLID)
@Uses(Icon.class)
public class GridwithFiltersView extends Div {

    private Grid<ArticleInfo> grid;
    private com.vaadin.flow.component.dialog.Dialog addDialog; // Lazy init für geringere Initialkosten
    private Filters filters;
    private final ArticleInfoService articleInfoService;

    public GridwithFiltersView(ArticleInfoService articleInfoService) {
        this.articleInfoService = articleInfoService;

        // === Grundlayout der Seite ===
        setSizeFull();
        addClassNames("gridwith-filters-view");

        // Filterleiste: ruft bei Änderungen/Buttons refreshGrid() auf
        filters = new Filters(this::refreshGrid);

        // Datengrid erzeugen (Spalten/Renderer/Selektor etc. im Helper kapseln)
        Component gridComponent = createGrid();

        // Datenquelle (Lazy/DataProvider) mit Grid verknüpfen
        setupDataProvider();

        // "Neuen Artikel" Button: öffnet (bzw. erzeugt + öffnet) das Dialogfenster
        Button addBtn = new Button("New Article", e -> {
            if (addDialog == null) {           // Lazy: nur bei Erstgebrauch bauen
                addDialog = buildAddDialog();  // Kapselung in Methode hält den ctor schlank
            }
            addDialog.open();
        });
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Toolbar rechtsbündig (Platz für weitere Aktionen)
        HorizontalLayout toolbar = new HorizontalLayout(addBtn);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        // === Seite zusammensetzen ===
        // 1) Mobile Filter-Kopf (toggle), 2) volle Filterleiste (Desktop/ausklappbar mobil),
        // 3) Toolbar mit "Neuen Artikel", 4) Grid
        VerticalLayout layout = new VerticalLayout(
                createMobileFilters(),
                filters,
                toolbar,
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
     *
     * Stellt Eingabefelder für Artikelname, Artikelnummer, Mindestbestand
     * und Lagerort bereit sowie Aktionen zum Suchen und Zurücksetzen.
     * Diese Klasse implementiert außerdem {@link Specification} für ArticleInfo,
     * sodass dieselben Feldwerte für die Datenbank-Filterung (JPA Criteria) genutzt werden können.
     *
     * - Platzhalter geben Beispielwerte an und reduzieren Fehleingaben.
     * - "Zurücksetzen" leert alle Felder und triggert sofort eine neue Suche.
     * - Artikelnummer nutzt ein IntegerField mit Step-Buttons und Min=0.
     *
     */
    public static class Filters extends Div implements Specification<ArticleInfo> {

        // Eingabekomponenten (sichtbare Filterfelder)
        private final TextField   articleName     = new TextField("Article Name");       // Freitext, case-insensitive LIKE
        private final IntegerField articleNumber  = new IntegerField("Article Number");  // Exakt gleich (=)
        private final TextField   inventory       = new TextField("Stock Level");           // Numerisch, >= Mindestbestand
        private final TextField   storageLocation = new TextField("Storage Location");          // Freitext, case-insensitive LIKE

        /**
         * Erstellt die Filterleiste und verbindet die Buttons mit der onSearch Suchaktion.
         *
         * onSearch Callback, der ausgeführt wird, wenn der Nutzer sucht oder zurücksetzt.
         *                 Lädt die Liste/Grids neu.
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
            articleNumber.setStepButtonsVisible(true); // Bessere Bedienbarkeit für Maus-/Touch-Nutzer
            articleNumber.setMin(0);                   // Fachliche Annahme: keine negativen Artikelnummern

            inventory.setPlaceholder("Minimum Inventory"); // Wird später als Integer geparst (mit Fallback)

            storageLocation.setPlaceholder("Search Storage Location");

            // === Aktionen ===

            // Leert alle Felder. Aktualisiert sofort
            // Anwender sieht Ungefiltertes Ergebnis
            Button resetBtn = new Button("Reset Search", e -> {
                articleName.clear();
                articleNumber.clear();
                inventory.clear();
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
            add(articleName, articleNumber, inventory, storageLocation, actions);
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
                ps.add(cb.like(cb.lower(root.get("articleName")), v));
            }

            // Exakte Übereinstimmung der Artikelnummer (Integer)
            if (articleNumber.getValue() != null) {
                ps.add(cb.equal(root.get("articleNumber"), articleNumber.getValue()));
            }

            // Mindestbestand: inventory >= eingegebener Wert
            if (!inventory.isEmpty()) {
                try {
                    int inv = Integer.parseInt(inventory.getValue().trim());
                    ps.add(cb.greaterThanOrEqualTo(root.get("inventory"), inv));
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
        grid.addColumn(ArticleInfo::getArticleName)
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
        grid.addColumn(ArticleInfo::getInventory)
                .setHeader("Stock Level")
                .setKey("inventory")
                .setAutoWidth(true)
                .setSortable(true);

        // Spalte: Lagerort (Text)
        grid.addColumn(ArticleInfo::getStorageLocation)
                .setHeader("Storage Location")
                .setKey("storageLocation")
                .setAutoWidth(true)
                .setSortable(true);

        // Optik/Lesbarkeit: verschiedenfarbige Streifen + Umbruch langer Inhalte
        grid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_WRAP_CELL_CONTENT
        );

        // Grid füllt verfügbaren Platz unter Div und Search feldern
        grid.setSizeFull();

        return grid;
    }

    /**
     * Baut den Dialog zum Anlegen eines neuen Artikels.
     * Enthält Formularfelder, Binder-Validierungen sowie Save/Cancel-Buttons.
     *
     * WICHTIG: AddButton ist ein Temporäres Feature.
     *          Aktuell nur zum hinzufügen eines Artikels zum testen.
     *
     * @return konfigurierter Dialog
     */
    private Dialog buildAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add new Article");

        // === Formularfelder ===

        TextField fName = new TextField("Article Name");
        fName.setRequired(true); // UI-Hinweis (optische Markierung)

        // Artikelnummer als IntegerField (bessere Validierung & Step-Buttons)
        IntegerField fNumber = new IntegerField("Article Number");
        fNumber.setRequiredIndicatorVisible(true);
        fNumber.setMin(0);                // fachliche Annahme: keine negativen Nummern
        fNumber.setStepButtonsVisible(true);

        IntegerField fInventory = new IntegerField("Stock Level");
        fInventory.setMin(0);             // Bestand >= 0
        fInventory.setStepButtonsVisible(true);
        fInventory.setValue(0);           // sinnvolle Voreinstellung

        TextField fStorage = new TextField("Storage Location");

        // Kompakte Formular-Anordnung
        FormLayout form = new FormLayout(fName, fNumber, fInventory, fStorage);
        dialog.add(form);

        // === Datenbindung & Validierung ===

        Binder<ArticleInfo> binder = new Binder<>(ArticleInfo.class);

        // Name: Pflichtfeld
        binder.forField(fName)
                .asRequired("Enter Article Name")
                .bind(ArticleInfo::getArticleName, ArticleInfo::setArticleName);

        // Nummer: Pflichtfeld (IntegerField liefert Integer/Null)
        binder.forField(fNumber)
                .asRequired("Enter Article Number")
                .bind(ArticleInfo::getArticleNumber, ArticleInfo::setArticleNumber);

        // Bestand: >= 0, optional aber validiert, falls gesetzt
        binder.forField(fInventory)
                .withValidator(v -> v != null && v >= 0, "Stock Level ≥ 0")
                .bind(ArticleInfo::getInventory, ArticleInfo::setInventory);

        // Lagerort: Pflichtfeld
        binder.forField(fStorage)
                .asRequired("Enter Storage Location")
                .bind(ArticleInfo::getStorageLocation, ArticleInfo::setStorageLocation);

        // === Aktionen ===

        Button cancel = new Button("Cancel", e -> dialog.close());

        Button save = new Button("Save", e -> {
            // Neues Bean befüllen
            ArticleInfo bean = new ArticleInfo();
            if (binder.writeBeanIfValid(bean)) {
                // Persistieren und UI aktualisieren
                articleInfoService.save(bean);
                dialog.close();
                refreshGrid();
                Notification.show("Article Saved");
            } else {
                // Mindestens eine Validierung ist fehlgeschlagen --> Popup
                Notification.show("Wrong Inputs. Please check Input fields");
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY); // visuelle Betonung

        dialog.getFooter().add(cancel, save);
        return dialog;
    }
    private void setupDataProvider() {
        DataProvider<ArticleInfo, Void> dataProvider = DataProvider.fromCallbacks(
                // FETCH
                (Query<ArticleInfo, Void> q) -> articleInfoService
                        .list(VaadinSpringDataHelpers.toSpringPageRequest(q), filters)
                        .stream(),
                // COUNT
                (Query<ArticleInfo, Void> q) -> (int) articleInfoService.count(filters)
        );

        grid.setDataProvider(dataProvider);
    }
    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}