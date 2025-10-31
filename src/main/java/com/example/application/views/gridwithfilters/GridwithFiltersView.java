package com.example.application.views.gridwithfilters;

import com.example.application.data.ArticleInfo;
import com.example.application.services.ArticleInfoService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
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
import java.util.Locale;

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
@PageTitle("Logistik")                 // Titel im Browser-Tab
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
        Button addBtn = new Button("Neuen Artikel", e -> {
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
        private final TextField   articleName     = new TextField("Artikelname");       // Freitext, case-insensitive LIKE
        private final IntegerField articleNumber  = new IntegerField("Artikelnummer");  // Exakt gleich (=)
        private final TextField   inventory       = new TextField("Bestand");           // Numerisch, >= Mindestbestand
        private final TextField   storageLocation = new TextField("Lagerort");          // Freitext, case-insensitive LIKE

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
            articleName.setPlaceholder("Name des Artikels");

            articleNumber.setPlaceholder("z. B. 12345");
            articleNumber.setStepButtonsVisible(true); // Bessere Bedienbarkeit für Maus-/Touch-Nutzer
            articleNumber.setMin(0);                   // Fachliche Annahme: keine negativen Artikelnummern

            inventory.setPlaceholder("Mindestbestand"); // Wird später als Integer geparst (mit Fallback)

            storageLocation.setPlaceholder("Lagerort eingeben");

            // === Aktionen ===

            // Leert alle Felder. Aktualisiert sofort
            // Anwender sieht Ungefiltertes Ergebnis
            Button resetBtn = new Button("Felder zurücksetzen", e -> {
                articleName.clear();
                articleNumber.clear();
                inventory.clear();
                storageLocation.clear();
                onSearch.run();
            });
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY); // Sekundäre/tertiäre Gewichtung im UI

            // Startet die Suche mit den aktuell eingegebenen Filterwerten.
            Button searchBtn = new Button("Artikel suchen", e -> onSearch.run());
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

    private Component createGrid() {

        grid = new Grid<>(ArticleInfo.class, false);

        grid.addColumn(ArticleInfo::getArticleName)
                .setHeader("Artikelname")
                .setKey("articleName")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(ArticleInfo::getArticleNumber)
                .setHeader("Artikelnummer")
                .setKey("articleNumber")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(ArticleInfo::getInventory)
                .setHeader("Bestand")
                .setKey("inventory")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(ArticleInfo::getStorageLocation)
                .setHeader("Lagerort")
                .setKey("storageLocation")
                .setAutoWidth(true)
                .setSortable(true);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setSizeFull();
        return grid;
    }
    private Dialog buildAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Neuen Artikel anlegen");

        TextField fName = new TextField("Artikelname");
        fName.setRequired(true);

        // Zahl statt Text
        IntegerField fNumber = new IntegerField("Artikelnummer");
        fNumber.setRequiredIndicatorVisible(true);
        fNumber.setMin(0);
        fNumber.setStepButtonsVisible(true);

        IntegerField fInventory = new IntegerField("Bestand");
        fInventory.setMin(0);
        fInventory.setStepButtonsVisible(true);
        fInventory.setValue(0);

        TextField fStorage = new TextField("Lagerort");

        FormLayout form = new FormLayout(fName, fNumber, fInventory, fStorage);
        dialog.add(form);

        Binder<ArticleInfo> binder = new Binder<>(ArticleInfo.class);

        binder.forField(fName)
                .asRequired("Bitte Artikelnamen angeben")
                .bind(ArticleInfo::getArticleName, ArticleInfo::setArticleName);

        binder.forField(fNumber)
                .asRequired("Bitte Artikelnummer angeben")
                .bind(ArticleInfo::getArticleNumber, ArticleInfo::setArticleNumber);

        binder.forField(fInventory)
                .withValidator(v -> v != null && v >= 0, "Bestand ≥ 0")
                .bind(ArticleInfo::getInventory, ArticleInfo::setInventory);

        binder.forField(fStorage)
                .asRequired("Bitte Lagerort angeben")
                .bind(ArticleInfo::getStorageLocation, ArticleInfo::setStorageLocation);


        Button cancel = new Button("Abbrechen", e -> dialog.close());
        Button save = new Button("Speichern", e -> {
            ArticleInfo bean = new ArticleInfo();
            if (binder.writeBeanIfValid(bean)) {
                articleInfoService.save(bean);
                dialog.close();
                refreshGrid();
                Notification.show("Artikel gespeichert");
            } else {
                Notification.show("Bitte Eingaben prüfen");
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
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