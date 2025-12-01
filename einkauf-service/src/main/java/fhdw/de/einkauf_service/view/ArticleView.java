package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import fhdw.de.einkauf_service.config.ShoppingCartSession;
import fhdw.de.einkauf_service.dto.*;
import fhdw.de.einkauf_service.service.ArticleCategoryService;
import fhdw.de.einkauf_service.service.ArticleService;
import fhdw.de.einkauf_service.service.SupplierService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Route(value = "articles", layout = MainLayout.class)
public class ArticleView extends VerticalLayout {

    private final ArticleService articleService;
    private final ShoppingCartSession cartSession;
    private final ArticleCategoryService categoryService;

    private final Grid<ArticleResponseDTO> grid = new Grid<>(ArticleResponseDTO.class);

    private final TextField articleNumberField = createSearchField("Artikelnummer (GTIN)");
    private final TextField nameField = createSearchField("Name");
    private final RadioButtonGroup<String> availabilityFilter = new RadioButtonGroup<>("Verfügbarkeit");
    private final ComboBox<SupplierResponseDTO> supplierBox = new ComboBox<>("Lieferant");
    private final MultiSelectComboBox<CategoryResponseDTO> categoryBox = new MultiSelectComboBox<>("Kategorie");

    private final Button clearButton = new Button("Suche abbrechen");
    private final Button addButton = new Button("Artikel hinzufügen");
    private final Button editButton = new Button("Bearbeiten");
    private final Button deleteButton = new Button("Löschen");
    private final Button addToCartButton = new Button("Zum Warenkorb hinzufügen", new Icon(VaadinIcon.CART));
    private final Button manageCategoriesButton = new Button("Kategorieverwaltung", new Icon(VaadinIcon.TAGS));

    private final Map<Long, SupplierResponseDTO> supplierCache;
    private final Map<Long, Integer> selectedArticles = new HashMap<>();

    public ArticleView(ArticleService articleService, SupplierService supplierService, ShoppingCartSession cartSession, ArticleCategoryService categoryService) {
        this.articleService = articleService;
        this.cartSession = cartSession;
        this.categoryService = categoryService;

        this.supplierCache = supplierService.findAllSuppliers().stream()
                .collect(Collectors.toMap(SupplierResponseDTO::getId, Function.identity(), (a, b) -> a));

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        add(new H2("Artikelverwaltung"));

        configureGrid();
        configureSearchComponents();
        configureCrudButtons();

        HorizontalLayout searchLayout = new HorizontalLayout(
                articleNumberField, nameField, supplierBox, categoryBox, availabilityFilter, clearButton
        );
        searchLayout.setAlignItems(Alignment.END);

        HorizontalLayout crudButtons = new HorizontalLayout(addButton, editButton, deleteButton, addToCartButton, manageCategoriesButton);
        manageCategoriesButton.getStyle().set("margin-left", "var(--lumo-space-l)");

        add(crudButtons, searchLayout, grid);

        updateList();
    }

    // ---------------------------------------
    // Grid-Konfiguration
    // ---------------------------------------
    private void configureGrid() {
        grid.setSizeFull();
        grid.setColumns();

        Checkbox headerCheckbox = new Checkbox();
        headerCheckbox.getElement().setProperty("title", "Alle auswählen/abwählen");

        grid.addComponentColumn(article -> {
            Checkbox checkbox = new Checkbox();
            checkbox.setValue(selectedArticles.containsKey(article.getId()));
            checkbox.addValueChangeListener(event -> {
                if (event.getValue()) selectedArticles.put(article.getId(), 1);
                else selectedArticles.remove(article.getId());
                grid.getDataProvider().refreshItem(article);
            });
            return checkbox;
        }).setHeader(headerCheckbox).setAutoWidth(true).setFlexGrow(0);

        headerCheckbox.addValueChangeListener(e -> {
            if (e.getValue()) grid.getListDataView().getItems().forEach(a -> selectedArticles.put(a.getId(), 1));
            else selectedArticles.clear();
            grid.getDataProvider().refreshAll();
        });

        grid.addColumn(ArticleResponseDTO::getArticleNumber).setHeader("Artikelnummer (GTIN)").setAutoWidth(true).setSortable(true);
        grid.addColumn(ArticleResponseDTO::getName).setHeader("Artikelname").setAutoWidth(true).setSortable(true);
        grid.addColumn(ArticleResponseDTO::getStockLevel).setHeader("Lagerbestand").setAutoWidth(true).setSortable(true);

        // Lieferanten-Spalte (nur Hauptlieferant anzeigen)
        grid.addColumn(article -> {
                    if (article.getMainSupplier() == null) return "-";
                    return article.getMainSupplier().getName();
                }).setHeader("Hauptlieferant")
                .setWidth("200px")
                .setFlexGrow(0)
                .setSortable(true);

        grid.addColumn(article -> {
            if (article.getCategoryIds() == null || article.getCategoryIds().isEmpty()) return "-";
            return article.getCategoryIds().stream()
                    .map(CategoryResponseDTO::getName)
                    .collect(Collectors.joining(", "));
        }).setHeader("Kategorie").setWidth("240px").setFlexGrow(0).setSortable(true);

        grid.addComponentColumn(article -> {
            Button infoButton = new Button(new Icon(VaadinIcon.ELLIPSIS_DOTS_H));
            infoButton.getStyle().set("background", "transparent");
            infoButton.getElement().setAttribute("title", "Weitere Artikelinfos anzeigen");
            infoButton.addClickListener(e -> showArticleDetails(article));
            return infoButton;
        }).setHeader("Weitere Infos").setAutoWidth(true).setFlexGrow(0);
    }

    // ---------------------------------------
    // Formular öffnen (Erstellen / Bearbeiten)
    // ---------------------------------------
    private void openArticleForm(ArticleResponseDTO article) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(article == null ? "Neuen Artikel hinzufügen" : "Artikel bearbeiten");

        TextField articleNumber = new TextField("Artikelnummer (GTIN)");
        TextField name = new TextField("Name");
        TextField stockLevel = new TextField("Lagerbestand");
        TextField purchasePrice = new TextField("EK-Preis (€)");
        TextField taxRate = new TextField("MwSt (%)");
        TextField marginPercent = new TextField("Marge (%)");
        TextField sellingPrice = new TextField("VK-Preis (€)");
        TextField manufacturer = new TextField("Hersteller");

        NumberField widthField = new NumberField("Breite (cm)");
        widthField.setMin(0);
        NumberField heightField = new NumberField("Höhe (cm)");
        heightField.setMin(0);
        NumberField depthField = new NumberField("Tiefe (cm)");
        depthField.setMin(0);

        ComboBox<SupplierResponseDTO> mainSupplierBox = new ComboBox<>("Hauptlieferant");
        MultiSelectComboBox<SupplierResponseDTO> suppliersSelect = new MultiSelectComboBox<>("Lieferanten");

        List<SupplierResponseDTO> allSuppliers = supplierCache.values().stream().sorted(Comparator.comparing(SupplierResponseDTO::getName)).collect(Collectors.toList());
        mainSupplierBox.setItems(allSuppliers);
        mainSupplierBox.setItemLabelGenerator(SupplierResponseDTO::getName);
        suppliersSelect.setItems(allSuppliers);
        suppliersSelect.setItemLabelGenerator(SupplierResponseDTO::getName);

        RadioButtonGroup<String> pfandRadio = new RadioButtonGroup<>("Pfand");
        pfandRadio.setItems("Ja", "Nein");
        RadioButtonGroup<String> availabilityRadio = new RadioButtonGroup<>("Verfügbarkeit");
        availabilityRadio.setItems("Verfügbar", "Nicht verfügbar");

        if (article == null) {
            // Beim Erstellen: immer "Nicht verfügbar" und nicht änderbar
            availabilityRadio.setValue("Nicht verfügbar");
            availabilityRadio.setEnabled(false);
        } else {
            // Beim Bearbeiten: auf den tatsächlichen Wert setzen und änderbar
            availabilityRadio.setValue(Boolean.TRUE.equals(article.getIsAvailable()) ? "Verfügbar" : "Nicht verfügbar");
            availabilityRadio.setEnabled(true);
        }

        MultiSelectComboBox<CategoryResponseDTO> categorySelect = new MultiSelectComboBox<>("Kategorie");
        List<CategoryResponseDTO> allCategories = categoryService.getAllCategories().stream()
                .sorted(Comparator.comparing(CategoryResponseDTO::getName))
                .collect(Collectors.toList());
        categorySelect.setItems(allCategories);
        categorySelect.setItemLabelGenerator(CategoryResponseDTO::getName);

        // Füllen bei Bearbeiten
        if (article != null) {
            articleNumber.setValue(safe(article.getArticleNumber()));
            name.setValue(safe(article.getName()));
            stockLevel.setValue(String.valueOf(article.getStockLevel()));
            purchasePrice.setValue(String.valueOf(article.getPurchasePrice()));
            taxRate.setValue(String.valueOf(article.getTaxRatePercent()));
            sellingPrice.setValue(String.valueOf(article.getSellingPrice()));
            manufacturer.setValue(safe(article.getManufacturer()));

            widthField.setValue(article.getWidthCm() != null ? article.getWidthCm() : 0.0);
            heightField.setValue(article.getHeightCm() != null ? article.getHeightCm() : 0.0);
            depthField.setValue(article.getDepthCm() != null ? article.getDepthCm() : 0.0);

            if (article.getMainSupplier() != null) mainSupplierBox.setValue(article.getMainSupplier());
            if (article.getSuppliers() != null) suppliersSelect.setValue(article.getSuppliers());

            categorySelect.setValue(article.getCategoryIds());
            pfandRadio.setValue(Boolean.TRUE.equals(article.getHasDeposit()) ? "Ja" : "Nein");
            availabilityRadio.setValue(Boolean.TRUE.equals(article.getIsAvailable()) ? "Verfügbar" : "Nicht verfügbar");
        }

        Button saveButton = new Button("Speichern", e -> {
            try {
                fhdw.de.einkauf_service.dto.ArticleRequestDTO req = new fhdw.de.einkauf_service.dto.ArticleRequestDTO();
                req.setArticleNumber(articleNumber.getValue());
                req.setName(name.getValue());
                req.setPurchasePrice(parseDouble(purchasePrice));
                req.setTaxRatePercent(parseDouble(taxRate));
                req.setSellingPrice(parseDouble(sellingPrice));
                req.setManufacturer(manufacturer.getValue());
                req.setStockLevel(Integer.parseInt(stockLevel.getValue()));
                req.setHasDeposit("Ja".equals(pfandRadio.getValue()));
                req.setIsAvailable("Verfügbar".equals(availabilityRadio.getValue()));
                req.setCategoryIds(categorySelect.getSelectedItems().stream().map(CategoryResponseDTO::getId).collect(Collectors.toSet()));

                // Lieferanten
                SupplierResponseDTO mainSupplier = mainSupplierBox.getValue();
                if (mainSupplier != null) req.setMainSupplierId(mainSupplier.getId());

                Set<SupplierResponseDTO> allSelectedSuppliers = suppliersSelect.getSelectedItems();
                if (allSelectedSuppliers != null) req.setSupplierIds(allSelectedSuppliers.stream().map(SupplierResponseDTO::getId).collect(Collectors.toSet()));

                // Neue Dimensionen
                req.setWidthCm(widthField.getValue());
                req.setHeightCm(heightField.getValue());
                req.setDepthCm(depthField.getValue());

                if (article == null) articleService.createNewArticle(req);
                else articleService.updateArticle(article.getId(), req);

                dialog.close();
                updateList();
            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("Fehler: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_START);
            }
        });
        Button cancelButton = new Button("Abbrechen", ev -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        VerticalLayout formLayout = new VerticalLayout(articleNumber, name, stockLevel, purchasePrice, taxRate, marginPercent,
                sellingPrice, manufacturer, widthField, heightField, depthField,
                mainSupplierBox, suppliersSelect, pfandRadio, availabilityRadio, categorySelect, buttons);
        formLayout.setPadding(false);
        formLayout.setSpacing(true);

        dialog.add(formLayout);
        dialog.open();
    }

    // ---------------------------------------
    // Filter / Suche
    // ---------------------------------------
    private void configureSearchComponents() {
        availabilityFilter.setItems("Verfügbar", "Nicht verfügbar");
        availabilityFilter.setValue("Verfügbar");

        supplierBox.setItems(supplierCache.values());
        supplierBox.setItemLabelGenerator(SupplierResponseDTO::getName);
        supplierBox.setClearButtonVisible(true);

        categoryBox.setItems(categoryService.getAllCategories());
        categoryBox.setItemLabelGenerator(CategoryResponseDTO::getName);
        categoryBox.setClearButtonVisible(true);

        articleNumberField.addValueChangeListener(e -> updateList());
        nameField.addValueChangeListener(e -> updateList());
        supplierBox.addValueChangeListener(e -> updateList());
        availabilityFilter.addValueChangeListener(e -> updateList());
        categoryBox.addValueChangeListener(e -> updateList());
        clearButton.addClickListener(e -> {
            articleNumberField.clear();
            nameField.clear();
            supplierBox.clear();
            availabilityFilter.setValue("Verfügbar");
            categoryBox.clear();
            updateList();
        });
    }

    private void updateList() {
        ArticleFilterDTO filter = new ArticleFilterDTO();
        filter.setArticleNumber(articleNumberField.getValue());
        filter.setName(nameField.getValue());

        SupplierResponseDTO supplierFilter = supplierBox.getValue();
        if (supplierFilter != null) filter.setSupplierId(supplierFilter.getId());

        String avail = availabilityFilter.getValue();
        filter.setIsAvailable("Verfügbar".equals(avail));

        Set<CategoryResponseDTO> selectedCats = categoryBox.getSelectedItems();
        if (selectedCats != null) filter.setCategoryIds(selectedCats.stream().map(CategoryResponseDTO::getId).collect(Collectors.toList()));

        List<ArticleResponseDTO> articles = articleService.findFilteredArticles(filter);
        grid.setItems(articles);
    }

    // ---------------------------------------
    // CRUD Buttons
    // ---------------------------------------
    private void configureCrudButtons() {
        addButton.addClickListener(e -> openArticleForm(null));
        editButton.addClickListener(e -> {
            ArticleResponseDTO selected = grid.asSingleSelect().getValue();
            if (selected != null) openArticleForm(selected);
        });
        deleteButton.addClickListener(e -> {
            ArticleResponseDTO selected = grid.asSingleSelect().getValue();
            if (selected != null) showDeleteArticleConfirmationDialog(selected);
        });
        addToCartButton.addClickListener(e -> addSelectedToCart());
    }

    private void addSelectedToCart() {
        if (selectedArticles.isEmpty()) {
            Notification.show("Bitte wählen Sie mindestens einen Artikel aus", 3000, Notification.Position.MIDDLE);
            return;
        }
        for (Long id : selectedArticles.keySet()) cartSession.addItem(id, 1);
        Notification.show("Artikel zum Warenkorb hinzugefügt", 2000, Notification.Position.BOTTOM_START);
        selectedArticles.clear();
        grid.getDataProvider().refreshAll();
    }

    private void showDeleteArticleConfirmationDialog(ArticleResponseDTO article) {
        Dialog confirm = new Dialog();
        confirm.setHeaderTitle("Löschen bestätigen");
        VerticalLayout content = new VerticalLayout(new Span("Artikel \"" + safe(article.getName()) + "\" löschen?"));
        Button confirmBtn = new Button("Löschen", ev -> {
            articleService.deleteArticle(article.getId());
            confirm.close();
            updateList();
            Notification.show("Artikel gelöscht", 2000, Notification.Position.BOTTOM_START);
        });
        Button cancelBtn = new Button("Abbrechen", ev -> confirm.close());
        content.add(new HorizontalLayout(confirmBtn, cancelBtn));
        confirm.add(content);
        confirm.open();
    }

    private void showArticleDetails(ArticleResponseDTO article) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Artikeldetails");

        VerticalLayout supplierLayout = new VerticalLayout();
        supplierLayout.setPadding(false);
        supplierLayout.setSpacing(false);

        if (article.getSuppliers() != null && !article.getSuppliers().isEmpty()) {
            for (SupplierResponseDTO s : article.getSuppliers()) {
                Span supplierSpan = new Span(s.getName());
                if (article.getMainSupplier() != null && s.getId().equals(article.getMainSupplier().getId())) {
                    supplierSpan.getStyle().set("font-weight", "bold");
                }
                supplierLayout.add(supplierSpan);
            }
        } else {
            supplierLayout.add(new Span("-"));
        }

        String categoriesText = article.getCategoryIds() == null ? "-" :
                article.getCategoryIds().stream().map(CategoryResponseDTO::getName).collect(Collectors.joining(", "));

        VerticalLayout content = new VerticalLayout(
                new Span("Artikelnummer (GTIN): " + safe(article.getArticleNumber())),
                new Span("Artikelname: " + safe(article.getName())),
                new Span("Lagerbestand: " + safe(article.getStockLevel())),
                new Span("EK-Preis (€): " + safe(article.getPurchasePrice())),
                new Span("VK-Preis (€): " + safe(article.getSellingPrice())),
                new Span("MwSt (%): " + safe(article.getTaxRatePercent())),
                new Span("Lieferant(en): "), supplierLayout,
                new Span("Kategorie: " + categoriesText),
                new Span("Pfand: " + (Boolean.TRUE.equals(article.getHasDeposit()) ? "Ja" : "Nein")),
                new Span("Verfügbar: " + (Boolean.TRUE.equals(article.getIsAvailable()) ? "Ja" : "Nein")),
                new Span("Abmessungen (B x H x T in cm): " +
                        safe(article.getWidthCm()) + " x " +
                        safe(article.getHeightCm()) + " x " +
                        safe(article.getDepthCm()))
        );

        dialog.add(content);
        dialog.getFooter().add(new Button("Schließen", ev -> dialog.close()));
        dialog.open();
    }

    private static TextField createSearchField(String label) {
        TextField tf = new TextField(label);
        tf.setClearButtonVisible(true);
        tf.setValueChangeMode(ValueChangeMode.EAGER);
        return tf;
    }

    private static String safe(Object o) {
        return o == null ? "-" : o.toString();
    }

    private static Double parseDouble(TextField field) {
        try { return Double.parseDouble(field.getValue().replace(",", ".")); }
        catch (Exception e) { return null; }
    }
}
