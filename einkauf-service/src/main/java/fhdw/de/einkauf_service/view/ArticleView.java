package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import fhdw.de.einkauf_service.dto.ArticleFilterDTO;
import fhdw.de.einkauf_service.dto.ArticleResponseDTO;
import fhdw.de.einkauf_service.service.ArticleService;

import java.util.List;

/**
 * Artikelansicht in Vaadin.
 * Bietet eine übersichtliche, filterbare Tabellenansicht aller Artikel:
 * • Grid zeigt nur ausgewählte Spalten.
 * • Dynamisch filterbar nach Artikelnummer, Name und Lieferant (Dropdown aus allen Lieferanten).
 * • Detaildialog mit allen Feldern beim Klick auf eine Zeile.
 */
@Route("") // Startseite unter http://localhost:8080
public class ArticleView extends VerticalLayout {

    // Service-Interface wird per Dependency Injection bereitgestellt (Spring)
    private final ArticleService articleService;
    // Grid für die Anzeige der Artikel
    private final Grid<ArticleResponseDTO> grid = new Grid<>(ArticleResponseDTO.class);

    // Suchfelder: Artikelnummer, Name, Lieferant als Dropdown (ComboBox)
    private final TextField articleNumberField = createSearchField("Artikelnummer (GTIN)");
    private final TextField nameField          = createSearchField("Name");
    private final ComboBox<String> supplierBox = new ComboBox<>("Lieferant");
    // Button zum Zurücksetzen aller Suchfelder
    private final Button clearButton  = new Button("Suche abbrechen");
    private final Button addButton = new Button("Artikel hinzufügen");
    private final Button editButton = new Button("Bearbeiten");
    private final Button deleteButton = new Button("Löschen");

    /**
     * Konstruktor – Initialisierung des Views.
     * - Richtet Grid ein
     * - Initialisiert die Filterfelder
     * - Lädt alle Lieferanten als Auswahlmöglichkeiten in die ComboBox
     * - Setzt Daten und UI-Layout
     */
    public ArticleView(ArticleService articleService) {
        this.articleService = articleService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        // Überschrift hinzufügen (optional für eine bessere Orientierung)
        add(new H2("Artikelverwaltung"));

        // Grid konfigurieren
        configureGrid();

        // Lieferanten-Dropdown initial einmalig befüllen
        supplierBox.setItems(articleService.findAllSupplierNames());
        supplierBox.setClearButtonVisible(true);

        // Filterfelder dynamisch verknüpfen
        configureSearchFields();

        // Suchfelder und Grid ins Layout einbauen
        HorizontalLayout searchLayout = new HorizontalLayout(
                articleNumberField, nameField, supplierBox, clearButton
        );

        HorizontalLayout crudButtons = new HorizontalLayout(addButton, editButton, deleteButton);
        add(crudButtons);
        configureCrudButtons();

        searchLayout.setAlignItems(Alignment.END);
        add(searchLayout, grid);




        // Initial: alle Artikel anzeigen
        updateList();
    }


    private void openArticleForm(ArticleResponseDTO article) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(article == null ? "Neuen Artikel hinzufügen" : "Artikel bearbeiten");

        // --- Input fields ---
        TextField articleNumber = new TextField("Artikelnummer (GTIN)");
        TextField name = new TextField("Name");
        TextField unit = new TextField("Einheit");
        TextField stockLevel = new TextField("Lagerbestand");
        TextField purchasePrice = new TextField("EK Preis");
        TextField taxRate = new TextField("MwSt (%)");
        TextField manufacturer = new TextField("Hersteller");
        ComboBox<String> supplier = new ComboBox<>("Lieferant");
        supplier.setItems(articleService.findAllSupplierNames());
        TextField description = new TextField("Beschreibung");

        // --- Prefill fields for update ---
        if (article != null) {
            articleNumber.setValue(safe(article.getArticleNumber()));
            name.setValue(safe(article.getName()));
            unit.setValue(safe(article.getUnit()));
            stockLevel.setValue(String.valueOf(article.getStockLevel()));
            purchasePrice.setValue(String.valueOf(article.getPurchasePrice()));
            taxRate.setValue(String.valueOf(article.getTaxRatePercent()));
            manufacturer.setValue(safe(article.getManufacturer()));
            supplier.setValue(article.getSupplier());
            description.setValue(safe(article.getDescription()));
        }

        // --- Buttons ---
        Button saveButton = new Button("Speichern", event -> {
            try {
                // Build request DTO
                fhdw.de.einkauf_service.dto.ArticleRequestDTO req = new fhdw.de.einkauf_service.dto.ArticleRequestDTO();
                req.setArticleNumber(articleNumber.getValue());
                req.setName(name.getValue());
                req.setUnit(unit.getValue());
                req.setPurchasePrice(Double.parseDouble(purchasePrice.getValue()));
                req.setTaxRatePercent(Double.parseDouble(taxRate.getValue()));
                req.setManufacturer(manufacturer.getValue());
                req.setSupplier(supplier.getValue());
                req.setStockLevel(Integer.parseInt(stockLevel.getValue()));
                req.setDescription(description.getValue());
                req.setIsAvailable(true);

                if (article == null) {
                    // CREATE
                    articleService.createNewArticle(req);
                } else {
                    // UPDATE
                    articleService.updateArticle(article.getId(), req);
                }

                dialog.close();
                updateList();
            } catch (Exception ex) {
                ex.printStackTrace();
                dialog.add(new Span("Fehler: " + ex.getMessage()));
            }
        });

        Button cancelButton = new Button("Abbrechen", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        VerticalLayout formLayout = new VerticalLayout(
                articleNumber, name, unit, stockLevel, purchasePrice,
                taxRate, manufacturer, supplier, description, buttons
        );
        formLayout.setPadding(false);
        formLayout.setSpacing(true);

        dialog.add(formLayout);
        dialog.open();
    }

    /**
     * Erstellt ein Textfeld mit Clear-Button und EAGER-Modus (sofortige Filterung)
     * @param label Feldbeschriftung
     */
    private static TextField createSearchField(String label) {
        TextField tf = new TextField(label);
        tf.setClearButtonVisible(true);
        tf.setValueChangeMode(ValueChangeMode.EAGER);
        return tf;
    }

    /**
     * Verknüpft alle Filterfelder so, dass eine Änderung jeweils updateList() auslöst.
     * Löscht der User alles über den "Suche abbrechen"-Button, werden alle Felder & Filter geleert.
     */
    private void configureSearchFields() {
        articleNumberField.addValueChangeListener(e -> updateList());
        nameField.addValueChangeListener(e -> updateList());
        supplierBox.addValueChangeListener(e -> updateList());
        clearButton.addClickListener(e -> {
            articleNumberField.clear();
            nameField.clear();
            supplierBox.clear();
            updateList();
        });
    }

    private void configureCrudButtons() {
        addButton.addClickListener(e -> openArticleForm(null)); // Create new
        editButton.addClickListener(e -> {
            ArticleResponseDTO selected = grid.asSingleSelect().getValue();
            if (selected != null) {
                openArticleForm(selected);
            }
        });
        deleteButton.addClickListener(e -> {
            ArticleResponseDTO selected = grid.asSingleSelect().getValue();
            if (selected != null) {
                articleService.deleteArticle(selected.getId());
                updateList();
            }
        });

        // Initially disabled until an article is selected
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        grid.asSingleSelect().addValueChangeListener(event -> {
            boolean hasSelection = event.getValue() != null;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });
    }

    /**
     * Konfiguration des Grids: Es werden nur Artikelnummer, Name, Einheit und Lagerbestand angezeigt.
     * Bei Klick auf eine Zeile öffnet sich der Detail-Dialog mit ALLEN Infos.
     */
    private void configureGrid() {
        grid.setSizeFull();
        grid.setColumns();
        grid.addColumn(ArticleResponseDTO::getArticleNumber).setHeader("Artikelnummer (GTIN)").setAutoWidth(true).setSortable(true);
        grid.addColumn(ArticleResponseDTO::getName).setHeader("Artikelname").setAutoWidth(true).setSortable(true);
        grid.addColumn(ArticleResponseDTO::getUnit).setHeader("Einheit").setAutoWidth(true).setSortable(true);
        grid.addColumn(ArticleResponseDTO::getStockLevel).setHeader("Lagerbestand").setAutoWidth(true).setSortable(true);

        // Detailansicht öffnen bei Klick
        grid.asSingleSelect().addValueChangeListener(event -> {
            ArticleResponseDTO selected = event.getValue();
            if (selected != null) {
                showArticleDetails(selected);
            }
        });
    }

    /**
     * Baut das Filter-DTO und ruft die gefilterte Artikelliste ab.
     * Es werden alle Kombis der Filterfelder gemeinsam verwendet (logisches AND).
     * Das Grid zeigt das Ergebnis dynamisch.
     */
    private void updateList() {
        ArticleFilterDTO filter = new ArticleFilterDTO();
        filter.setArticleNumber(articleNumberField.getValue());
        filter.setName(nameField.getValue());
        filter.setSupplier(supplierBox.getValue());
        filter.setIsAvailable(true);

        List<ArticleResponseDTO> articles = articleService.findFilteredArticles(filter);
        grid.setItems(articles);
    }

    /**
     * Öffnet den Detail-Dialog zu einem Artikel, der alle Felder ausgibt.
     * @param article Das selektierte ArticleResponseDTO.
     */
    private void showArticleDetails(ArticleResponseDTO article) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Artikeldetails");

        VerticalLayout detailsLayout = new VerticalLayout(
                new Span("Artikelnummer (GTIN): "       + safe(article.getArticleNumber())),
                new Span("Artikelname: "                + safe(article.getName())),
                new Span("Einheit: "                    + safe(article.getUnit())),
                new Span("Lagerbestand: "               + safe(article.getStockLevel())),
                new Span("EK Preis: "                   + safe(article.getPurchasePrice())),
                new Span("VK Preis: "                   + safe(article.getSellingPrice())),
                new Span("MwSt (%): "                   + safe(article.getTaxRatePercent())),
                new Span("Lieferant: "                  + safe(article.getSupplier())),
                new Span("Verfügbar: "                  + safe(article.getIsAvailable())),
                new Span("Beschreibung / Produktdetails: " + safe(article.getDescription()))
        );

        dialog.add(detailsLayout);
        dialog.getFooter().add(new Button("Schließen", e -> dialog.close()));
        dialog.open();
    }

    /**
     * Gibt einen Strich zurück, wenn der Wert null ist – für die Detailanzeige.
     */
    private String safe(Object value) {
        return value == null ? "-" : value.toString();
    }
}