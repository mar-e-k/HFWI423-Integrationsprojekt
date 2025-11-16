package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import fhdw.de.einkauf_service.config.ShoppingCartSession;
import fhdw.de.einkauf_service.dto.ArticleFilterDTO;
import fhdw.de.einkauf_service.dto.ArticleResponseDTO;
import fhdw.de.einkauf_service.dto.SupplierResponseDTO;
import fhdw.de.einkauf_service.service.ArticleService;
import fhdw.de.einkauf_service.service.SupplierService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Route(value = "articles", layout = MainLayout.class)
public class ArticleView extends VerticalLayout {

    private final ArticleService articleService;
    private final ShoppingCartSession cartSession;
    private final Grid<ArticleResponseDTO> grid = new Grid<>(ArticleResponseDTO.class);

    private final TextField articleNumberField = createSearchField("Artikelnummer (GTIN)");
    private final TextField nameField = createSearchField("Name");

    private final ComboBox<SupplierResponseDTO> supplierBox = new ComboBox<>("Lieferant");

    private final Button clearButton = new Button("Suche abbrechen");
    private final Button addButton = new Button("Artikel hinzufügen");
    private final Button editButton = new Button("Bearbeiten");
    private final Button deleteButton = new Button("Löschen");
    private final Button addToCartButton = new Button("Zum Warenkorb hinzufügen", new Icon(VaadinIcon.CART));


    private final Map<Long, SupplierResponseDTO> supplierCache;
    private final Map<Long, Integer> selectedArticles = new HashMap<>();

    public ArticleView(ArticleService articleService, SupplierService supplierService, ShoppingCartSession cartSession) {
        this.articleService = articleService;
        this.cartSession = cartSession;
        this.supplierCache = supplierService.findAllSuppliers().stream()
                .collect(Collectors.toMap(SupplierResponseDTO::getId, Function.identity(), (a, b) -> a));

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        add(new H2("Artikelverwaltung"));

        configureGrid();

        supplierBox.setItems(supplierCache.values());
        supplierBox.setItemLabelGenerator(SupplierResponseDTO::getName);
        supplierBox.setClearButtonVisible(true);

        configureSearchFields();

        HorizontalLayout searchLayout = new HorizontalLayout(
                articleNumberField, nameField, supplierBox, clearButton
        );

        HorizontalLayout crudButtons = new HorizontalLayout(addButton, editButton, deleteButton,addToCartButton);
        add(crudButtons);
        configureCrudButtons();

        searchLayout.setAlignItems(Alignment.END);
        add(searchLayout, grid);

        updateList();
    }

    // ----------------------------------------------------------------------------------
    // Artikel Formular (Erstellen / Bearbeiten)
    // ----------------------------------------------------------------------------------

    private void openArticleForm(ArticleResponseDTO article) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(article == null ? "Neuen Artikel hinzufügen" : "Artikel bearbeiten");

        // --- Input fields ---
        TextField articleNumber = new TextField("Artikelnummer (GTIN)");
        TextField name = new TextField("Name");
        TextField stockLevel = new TextField("Lagerbestand");
        TextField purchasePrice = new TextField("EK Preis");
        TextField taxRate = new TextField("MwSt (%)");
        TextField manufacturer = new TextField("Hersteller");

        // Formular-ComboBox verwaltet SupplierResponseDTOs
        ComboBox<SupplierResponseDTO> supplierBoxForm = new ComboBox<>("Lieferant");
        supplierBoxForm.setItems(supplierCache.values());
        supplierBoxForm.setItemLabelGenerator(SupplierResponseDTO::getName);

        TextField description = new TextField("Beschreibung");

        // --- Prefill fields for update ---
        if (article != null) {
            articleNumber.setValue(safe(article.getArticleNumber()));
            name.setValue(safe(article.getName()));
            stockLevel.setValue(String.valueOf(article.getStockLevel()));
            purchasePrice.setValue(String.valueOf(article.getPurchasePrice()));
            taxRate.setValue(String.valueOf(article.getTaxRatePercent()));
            manufacturer.setValue(safe(article.getManufacturer()));

            // Auflösen der ID aus dem ResponseDTO (article) in das DTO-Objekt aus dem Cache
            if (article.getSupplierId() != null) {
                SupplierResponseDTO currentSupplierDto = supplierCache.get(article.getSupplierId());
                if (currentSupplierDto == null) {
                    throw new IllegalStateException("Lieferant-ID im Artikel-DTO (" + article.getSupplierId() + ") ist ungültig.");
                }
                supplierBoxForm.setValue(currentSupplierDto);
            }
            description.setValue(safe(article.getDescription()));
        }

        // --- Buttons ---
        Button saveButton = new Button("Speichern", event -> {
            try {
                // Build request DTO
                fhdw.de.einkauf_service.dto.ArticleRequestDTO req = new fhdw.de.einkauf_service.dto.ArticleRequestDTO();
                req.setArticleNumber(articleNumber.getValue());
                req.setName(name.getValue());
                req.setPurchasePrice(Double.parseDouble(purchasePrice.getValue()));
                req.setTaxRatePercent(Double.parseDouble(taxRate.getValue()));
                req.setManufacturer(manufacturer.getValue());

                SupplierResponseDTO selectedSupplierDto = supplierBoxForm.getValue();

                if (selectedSupplierDto != null) {
                    // Schreibt die ID des DTOs in das Request DTO
                    req.setSupplierId(selectedSupplierDto.getId());
                } else {
                    Notification.show("Bitte wählen Sie einen Lieferanten aus.");
                    return;
                }
                req.setStockLevel(Integer.parseInt(stockLevel.getValue()));
                req.setDescription(description.getValue());
                req.setIsAvailable(true);

                if (article == null) {
                    articleService.createNewArticle(req);
                } else {
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
                articleNumber, name, stockLevel, purchasePrice,
                taxRate, manufacturer, supplierBoxForm, description, buttons
        );
        formLayout.setPadding(false);
        formLayout.setSpacing(true);

        dialog.add(formLayout);
        dialog.open();
    }
    // ----------------------------------------------------------------------------------
    // Filterung und Grid-Update
    // ----------------------------------------------------------------------------------

    private void updateList() {
        ArticleFilterDTO filter = new ArticleFilterDTO();
        filter.setArticleNumber(articleNumberField.getValue());
        filter.setName(nameField.getValue());

        // KORRIGIERT: Filterung übergibt die ID an das Backend
        SupplierResponseDTO selectedSupplierFilter = supplierBox.getValue();
        if (selectedSupplierFilter != null) {
            // Annahme: ArticleFilterDTO hat ein Long supplierId Feld
            filter.setSupplierId(selectedSupplierFilter.getId());
        } else {
            filter.setSupplierId(null);
        }

        filter.setIsAvailable(true);

        List<ArticleResponseDTO> articles = articleService.findFilteredArticles(filter);
        grid.setItems(articles);
    }

    // ----------------------------------------------------------------------------------
    // Sonstige Hilfsmethoden
    // ----------------------------------------------------------------------------------

    private void configureGrid() {
        grid.setSizeFull();
        grid.setColumns();
                //Checkbox Spalte
        grid.addComponentColumn(article -> {
        Checkbox checkbox = new Checkbox();
        checkbox.setValue(selectedArticles.containsKey(article.getId()));
        
        checkbox.addValueChangeListener(event -> {
            if (event.getValue()) {
                selectedArticles.put(article.getId(), 1);
            } else {
                selectedArticles.remove(article.getId());
            }
            grid.getDataProvider().refreshItem(article);
        });
        
        return checkbox;
    }).setHeader("✓").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(ArticleResponseDTO::getArticleNumber).setHeader("Artikelnummer (GTIN)").setAutoWidth(true).setSortable(true);
        grid.addColumn(ArticleResponseDTO::getName).setHeader("Artikelname").setAutoWidth(true).setSortable(true);
        grid.addColumn(ArticleResponseDTO::getStockLevel).setHeader("Lagerbestand").setAutoWidth(true).setSortable(true);

        // KORRIGIERT: Zeigt den DTO-Namen an (Backend MUSS nun getSupplierName im ArticleResponseDTO liefern)
        grid.addColumn(ArticleResponseDTO::getSupplierName).setHeader("Lieferant").setAutoWidth(true).setSortable(true);

        // Detailansicht öffnen bei Klick
        grid.asSingleSelect().addValueChangeListener(event -> {
            ArticleResponseDTO selected = event.getValue();
            if (selected != null) {
                showArticleDetails(selected);
            }
        });


    }


    private void showArticleDetails(ArticleResponseDTO article) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Artikeldetails");

        VerticalLayout detailsLayout = new VerticalLayout(
                new Span("Artikelnummer (GTIN): " + safe(article.getArticleNumber())),
                new Span("Artikelname: " + safe(article.getName())),
                new Span("Lagerbestand: " + safe(article.getStockLevel())),
                new Span("EK Preis: " + safe(article.getPurchasePrice())),
                new Span("VK Preis: " + safe(article.getSellingPrice())),
                new Span("MwSt (%): " + safe(article.getTaxRatePercent())),
                // KORRIGIERT: Nutzt den String supplierName aus dem ArticleResponseDTO
                new Span("Lieferant: " + safe(article.getSupplierName())),
                new Span("Verfügbar: " + safe(article.getIsAvailable())),
                new Span("Beschreibung / Produktdetails: " + safe(article.getDescription()))
        );

        dialog.add(detailsLayout);
        dialog.getFooter().add(new Button("Schließen", e -> dialog.close()));
        dialog.open();
    }

    private static TextField createSearchField(String label) {
        TextField tf = new TextField(label);
        tf.setClearButtonVisible(true);
        tf.setValueChangeMode(ValueChangeMode.EAGER);
        return tf;
    }

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
        addButton.addClickListener(e -> openArticleForm(null));
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
        addToCartButton.addClickListener(e -> addSelectedToCart());

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        grid.asSingleSelect().addValueChangeListener(event -> {
            boolean hasSelection = event.getValue() != null;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });
    }
    private void addSelectedToCart() {
    if (selectedArticles.isEmpty()) {
        Notification.show("Bitte wählen Sie mindestens einen Artikel aus", 3000, Notification.Position.MIDDLE);
        return;
    }

    // Alle ausgewählten Artikel  in den Warenkorb
    for (Long articleId : selectedArticles.keySet()) {
        cartSession.addItem(articleId, 1);
    }

    Notification.show("Artikel zum Warenkorb hinzugefügt", 2000, Notification.Position.BOTTOM_START);

    // Auswahl zurücksetzen und Grid aktualisieren
    selectedArticles.clear();
    grid.getDataProvider().refreshAll();
}

    private String safe(Object value) {
        return value == null ? "-" : value.toString();
    }
}