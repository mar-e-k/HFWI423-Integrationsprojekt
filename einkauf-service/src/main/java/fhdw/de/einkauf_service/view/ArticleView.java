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
        searchLayout.setAlignItems(Alignment.END);

        add(searchLayout, grid);

        // Initial: alle Artikel anzeigen
        updateList();
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