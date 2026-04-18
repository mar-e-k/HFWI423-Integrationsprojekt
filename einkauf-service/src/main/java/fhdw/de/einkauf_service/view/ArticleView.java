package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import fhdw.de.einkauf_service.config.ShoppingCartSession;


import fhdw.de.einkauf_service.dto.ArticleFilterDTO;
import fhdw.de.einkauf_service.dto.ArticleResponseDTO;
import fhdw.de.einkauf_service.dto.CategoryResponseDTO;
import fhdw.de.einkauf_service.dto.SupplierResponseDTO;
import fhdw.de.einkauf_service.service.ArticleCategoryService;
import fhdw.de.einkauf_service.service.ArticleService;
import fhdw.de.einkauf_service.service.SupplierService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Route(value = "articles", layout = MainLayout.class)
public class ArticleView extends VerticalLayout {

    private final ArticleService articleService;
    private final ShoppingCartSession cartSession;
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
    private final List<CategoryResponseDTO> categoryCache;
    private final Map<Long, Integer> selectedArticles = new HashMap<>();

    private final ArticleCategoryService categoryService;

    public ArticleView(ArticleService articleService, SupplierService supplierService, ShoppingCartSession cartSession, ArticleCategoryService categoryService) {
        this.articleService = articleService;
        this.cartSession = cartSession;
        this.categoryService = categoryService;
        this.supplierCache = supplierService.findAllSuppliers().stream()
                .collect(Collectors.toMap(SupplierResponseDTO::getId, Function.identity(), (a, b) -> a));
        this.categoryCache = categoryService.getAllCategories();
        this.categoryCache.sort((a, b) -> {
            String n1 = a.getName() != null ? a.getName() : "";
            String n2 = b.getName() != null ? b.getName() : "";
            return n1.compareToIgnoreCase(n2);
        });

        setSizeFull();
        addClassName("page-view");

        add(new H2("Artikelverwaltung"));

        configureGrid();

        List<SupplierResponseDTO> allSuppliers = supplierCache.values().stream()
                .collect(Collectors.toList());   // modifizierbare Liste
        allSuppliers.sort((a, b) -> {
            String n1 = a.getName() != null ? a.getName() : "";
            String n2 = b.getName() != null ? b.getName() : "";
            return n1.compareToIgnoreCase(n2);
        });
        supplierBox.setItems(allSuppliers);
        supplierBox.setItemLabelGenerator(SupplierResponseDTO::getName);
        supplierBox.setClearButtonVisible(true);

        availabilityFilter.setItems("Verfügbar", "Nicht verfügbar");
        availabilityFilter.setValue("Verfügbar");

        categoryBox.setItems(categoryCache);
        categoryBox.setItemLabelGenerator(CategoryResponseDTO::getName);
        categoryBox.setClearButtonVisible(true);

        configureSearchFields();

        HorizontalLayout searchLayout = new HorizontalLayout(
                articleNumberField, nameField, supplierBox, categoryBox, availabilityFilter, clearButton
        );
        searchLayout.addClassName("search-toolbar");

        HorizontalLayout crudButtons = new HorizontalLayout(addButton, editButton, deleteButton, addToCartButton, manageCategoriesButton);
        manageCategoriesButton.getStyle().set("margin-left", "var(--lumo-space-l)");
        crudButtons.addClassName("action-toolbar");
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

        TextField articleNumber = new TextField("Artikelnummer (GTIN)");
        articleNumber.setRequired(true);
        TextField name = new TextField("Name");
        name.setRequired(true);
        TextField stockLevel = new TextField("Lagerbestand");
        stockLevel.setRequired(true);
        TextField purchasePrice = new TextField("EK-Preis (€)");
        purchasePrice.setRequired(true);
        TextField taxRate = new TextField("MwSt (%)");
        taxRate.setRequired(true);
        TextField marginPercent = new TextField("Marge (%)");
        marginPercent.setRequired(true);
        TextField sellingPrice = new TextField("VK-Preis (€)");
        sellingPrice.setReadOnly(true);
        TextField manufacturer = new TextField("Hersteller");
        manufacturer.setRequired(true);

        purchasePrice.setValueChangeMode(ValueChangeMode.EAGER);
        taxRate.setValueChangeMode(ValueChangeMode.EAGER);
        marginPercent.setValueChangeMode(ValueChangeMode.EAGER);

        // Hauptlieferant (nur einer auswählbar)
        ComboBox<SupplierResponseDTO> mainSupplierBox = new ComboBox<>("Hauptlieferant");
        List<SupplierResponseDTO> allSuppliersForm = supplierCache.values().stream()
                .sorted(Comparator.comparing(s -> s.getName() != null ? s.getName() : ""))
                .collect(Collectors.toList());
        mainSupplierBox.setItems(allSuppliersForm);
        mainSupplierBox.setItemLabelGenerator(SupplierResponseDTO::getName);
        mainSupplierBox.setRequired(true); // falls Pflichtfeld

// Weitere Lieferanten (mehrere auswählbar)
        MultiSelectComboBox<SupplierResponseDTO> suppliersSelect = new MultiSelectComboBox<>("Weitere Lieferanten");
        suppliersSelect.setItems(allSuppliersForm);
        suppliersSelect.setItemLabelGenerator(SupplierResponseDTO::getName);

        RadioButtonGroup<String> pfandRadio = new RadioButtonGroup<>("Pfand");
        pfandRadio.setItems("Ja", "Nein");
        if (article != null) {
            pfandRadio.setValue(Boolean.TRUE.equals(article.getHasDeposit()) ? "Ja" : "Nein");
        } else {
            pfandRadio.setValue("Nein");
        }
        pfandRadio.setRequired(true);

        RadioButtonGroup<String> availabilityRadio = new RadioButtonGroup<>("Verfügbarkeit");
        availabilityRadio.setItems("Verfügbar", "Nicht verfügbar");
        if (article != null) {
            // Bearbeiten: enabled, mit aktuellem Wert
            availabilityRadio.setValue(Boolean.TRUE.equals(article.getAvailable()) ? "Verfügbar" : "Nicht verfügbar");
            availabilityRadio.setEnabled(true);
        } else {
            // Erstellen: disabled, standardmäßig "Nicht verfügbar"
            availabilityRadio.setValue("Nicht verfügbar");
            availabilityRadio.setEnabled(false);
        }
        availabilityRadio.setRequired(true);

        MultiSelectComboBox<CategoryResponseDTO> categorySelect =
                new MultiSelectComboBox<>("Kategorie");
        List<CategoryResponseDTO> allCategories = categoryCache;
        categorySelect.setItems(allCategories);
        categorySelect.setItemLabelGenerator(CategoryResponseDTO::getName);
        categorySelect.setRequired(true);
        categorySelect.setRequiredIndicatorVisible(true);

        TextField description = new TextField("Beschreibung");
        TextField depthCm = new TextField("Tiefe (cm)");
        depthCm.setRequired(true);
        TextField heightCm = new TextField("Höhe (cm)");
        heightCm.setRequired(true);
        TextField widthCm = new TextField("Breite (cm)");
        widthCm.setRequired(true);

        TextField productImageUrl = new TextField("Produktbild-URL");
        productImageUrl.setPlaceholder("z.B. https://www.rossmann.de/media-neu/...");
        productImageUrl.setRequired(true);

        DatePicker expirationDate = new DatePicker("Mindesthaltbarkeitsdatum (optional)");
        expirationDate.setPlaceholder("Wählen Sie ein Datum...");

        java.util.function.Function<TextField, Double> parseDoubleOrNull = field -> {
            try {
                String v = field.getValue();
                if (v == null || v.trim().isEmpty()) {
                    return null;
                }
                return Double.parseDouble(v.replace(",", ".").trim());
            } catch (NumberFormatException e) {
                return null;
            }
        };

        Runnable recalcSellingPrice = () -> {
            Double ek = parseDoubleOrNull.apply(purchasePrice);
            Double mwst = parseDoubleOrNull.apply(taxRate);
            Double marge = parseDoubleOrNull.apply(marginPercent);

            if (ek == null || mwst == null || marge == null || ek <= 0 || mwst <= 0) {
                sellingPrice.clear();
                return;
            }

            double nettoMitMarge = ek * (1 + marge / 100.0);
            double vkRaw = nettoMitMarge * (1 + mwst / 100.0);
            double vk = Math.round(vkRaw * 100.0) / 100.0;

            sellingPrice.setValue(String.format("%.2f", vk));
        };

        purchasePrice.addValueChangeListener(e -> recalcSellingPrice.run());
        taxRate.addValueChangeListener(e -> recalcSellingPrice.run());
        marginPercent.addValueChangeListener(e -> recalcSellingPrice.run());

        if (article != null) {
            articleNumber.setValue(safe(article.getArticleNumber()));
            name.setValue(safe(article.getName()));
            stockLevel.setValue(String.valueOf(article.getStockLevel()));
            purchasePrice.setValue(String.valueOf(article.getPurchasePrice()));
            taxRate.setValue(String.valueOf(article.getTaxRatePercent()));
            sellingPrice.setValue(String.valueOf(article.getSellingPrice()));
            manufacturer.setValue(safe(article.getManufacturer()));

            // Hauptlieferant setzen
            if (article.getMainSupplier() != null) {
                mainSupplierBox.setValue(article.getMainSupplier());
            }

            // Weitere Lieferanten setzen
            if (article.getSuppliers() != null && !article.getSuppliers().isEmpty()) {
                suppliersSelect.setValue(article.getSuppliers());
            }


            if (article.getCategoryIds() != null && !article.getCategoryIds().isEmpty()) {

                Set<Long> articleCategoryIds = article.getCategoryIds().stream()
                        .map(CategoryResponseDTO::getId)
                        .collect(Collectors.toSet());

                Set<CategoryResponseDTO> preselected = allCategories.stream()
                        .filter(c -> articleCategoryIds.contains(c.getId()))
                        .collect(Collectors.toSet());

                categorySelect.setValue(preselected);
            }

            description.setValue(safe(article.getDescription()));
            depthCm.setValue(String.valueOf(article.getDepthCm()));
            heightCm.setValue(String.valueOf(article.getHeightCm()));
            widthCm.setValue(String.valueOf(article.getWidthCm()));
            if (article.getProductImage() != null) {
                productImageUrl.setValue(article.getProductImage());
            }
            if (article.getExpirationDate() != null) {
                expirationDate.setValue(article.getExpirationDate());
            }

            Double ek = article.getPurchasePrice();
            Double mwst = article.getTaxRatePercent();
            Double vk = article.getSellingPrice();

            double nettoVk = vk / (1 + mwst / 100.0);
            double marge = (nettoVk / ek - 1.0) * 100.0;
            double margeRounded = Math.round(marge * 100.0) / 100.0;
            marginPercent.setValue(String.format("%.2f", margeRounded));
        }

        // --- Buttons ---
        Button saveButton = new Button("Speichern", event -> {
            try {
                if (articleNumber.isEmpty() || name.isEmpty() || stockLevel.isEmpty()
                        || purchasePrice.isEmpty() || taxRate.isEmpty()
                        || manufacturer.isEmpty() || mainSupplierBox.isEmpty()
                        || depthCm.isEmpty() || heightCm.isEmpty() || widthCm.isEmpty()
                        || marginPercent.isEmpty()) {
                    throw new IllegalArgumentException("Alle Pflichtfelder müssen ausgefüllt werden.");
                }

                if (categorySelect.getSelectedItems() == null
                        || categorySelect.getSelectedItems().isEmpty()) {
                    Notification.show("Bitte wählen Sie mindestens eine Kategorie aus.", 4000,
                            Notification.Position.BOTTOM_START);
                    return;
                }

                Double ek = parseDoubleOrNull.apply(purchasePrice);
                Double mwst = parseDoubleOrNull.apply(taxRate);
                Double marge = parseDoubleOrNull.apply(marginPercent);

                if (ek == null || mwst == null || marge == null || ek <= 0 || mwst <= 0) {
                    Notification.show("Bitte gültige Zahlen für EK, MwSt und Marge eingeben.", 4000,
                            Notification.Position.BOTTOM_START);
                    return;
                }

                double nettoMitMarge = ek * (1 + marge / 100.0);
                double vkRaw = nettoMitMarge * (1 + mwst / 100.0);
                BigDecimal vkBD = BigDecimal.valueOf(vkRaw).setScale(2, RoundingMode.HALF_UP);
                double vk = vkBD.doubleValue();

                fhdw.de.einkauf_service.dto.ArticleRequestDTO req =
                        new fhdw.de.einkauf_service.dto.ArticleRequestDTO();
                req.setArticleNumber(articleNumber.getValue());
                req.setName(name.getValue());
                req.setPurchasePrice(ek);
                sellingPrice.setValue(vkBD.toPlainString());
                req.setSellingPrice(vk);
                req.setTaxRatePercent(mwst);
                req.setManufacturer(manufacturer.getValue());

                // Hauptlieferant
                SupplierResponseDTO mainSupplier = mainSupplierBox.getValue();
                if (mainSupplier != null) {
                    req.setMainSupplierId(mainSupplier.getId());
                } else {
                    Notification.show("Bitte wählen Sie einen Hauptlieferanten aus.");
                    return;
                }

                // Weitere Lieferanten
                Set<SupplierResponseDTO> additionalSuppliers = suppliersSelect.getSelectedItems();
                if (additionalSuppliers != null && !additionalSuppliers.isEmpty()) {
                    req.setSupplierIds(additionalSuppliers.stream()
                            .map(SupplierResponseDTO::getId)
                            .collect(Collectors.toSet()));
                }

                req.setStockLevel(Integer.parseInt(stockLevel.getValue()));
                req.setDescription(description.getValue());
                req.setAvailable("Verfügbar".equals(availabilityRadio.getValue()));
                req.setHasDeposit("Ja".equals(pfandRadio.getValue()));
                req.setDepthCm(Double.parseDouble(depthCm.getValue()));
                req.setHeightCm(Double.parseDouble(heightCm.getValue()));
                req.setWidthCm(Double.parseDouble(widthCm.getValue()));
                req.setProductImage(productImageUrl.getValue());
                req.setExpirationDate(expirationDate.getValue());

                Set<Long> categoryIds = categorySelect.getSelectedItems().stream()
                        .map(CategoryResponseDTO::getId)
                        .collect(Collectors.toSet());
                req.setCategoryIds(categoryIds);

                if (article == null) {
                    articleService.createNewArticle(req);
                } else {
                    articleService.updateArticle(article.getId(), req);
                }

                dialog.close();
                updateList();
            } catch (Exception ex) {
                ex.printStackTrace();
                Span errorMsg = new Span("Fehler: " + ex.getMessage());
                errorMsg.getStyle().set("color", "red");
                dialog.add(errorMsg);
            }
        });


        Button cancelButton = new Button("Abbrechen", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        VerticalLayout formLayout = new VerticalLayout(
                articleNumber, name, stockLevel, purchasePrice, taxRate, marginPercent, sellingPrice,
                manufacturer, mainSupplierBox, suppliersSelect, pfandRadio, availabilityRadio, categorySelect, description,
                widthCm, heightCm, depthCm, productImageUrl, expirationDate, buttons
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

        SupplierResponseDTO selectedSupplierFilter = supplierBox.getValue();
        if (selectedSupplierFilter != null) {
            filter.setSupplierId(selectedSupplierFilter.getId());
        } else {
            filter.setSupplierId(null);
        }

        // Setze Verfügbarkeitsfilter basierend auf der Auswahl
        String selectedAvailability = availabilityFilter.getValue();
        if ("Verfügbar".equals(selectedAvailability)) {
            filter.setAvailable(true);
        } else if ("Nicht verfügbar".equals(selectedAvailability)) {
            filter.setAvailable(false);
        }

        Set<CategoryResponseDTO> selectedCategories = categoryBox.getSelectedItems();
        if (selectedCategories != null && !selectedCategories.isEmpty()) {
            filter.setCategoryIds(
                    selectedCategories.stream()
                            .map(CategoryResponseDTO::getId)
                            .collect(Collectors.toList())
            );
        } else {
            filter.setCategoryIds(null);
        }

        List<ArticleResponseDTO> articles = articleService.findFilteredArticles(filter);
        grid.setItems(articles);
    }

    // ----------------------------------------------------------------------------------
    // Sonstige Hilfsmethoden
    // ----------------------------------------------------------------------------------

    private void configureGrid() {
        grid.setSizeFull();
        grid.setColumns();

        Checkbox headerCheckbox = new Checkbox();
        headerCheckbox.getElement().setProperty("title", "Alle auswählen/abwählen");

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
                })
                .setHeader(headerCheckbox)
                .setAutoWidth(true)
                .setFlexGrow(0);

        headerCheckbox.addValueChangeListener(e -> {
            if (e.getValue()) {
                grid.getListDataView().getItems()
                        .forEach(article -> selectedArticles.put(article.getId(), 1));
            } else {
                selectedArticles.clear();
            }
            grid.getDataProvider().refreshAll();
        });

        grid.addColumn(ArticleResponseDTO::getArticleNumber).setHeader("Artikelnummer (GTIN)").setAutoWidth(true).setSortable(true);
        grid.addColumn(ArticleResponseDTO::getName).setHeader("Artikelname").setAutoWidth(true).setSortable(true);
        grid.addColumn(ArticleResponseDTO::getStockLevel).setHeader("Lagerbestand").setAutoWidth(true).setSortable(true);

        grid.addColumn(article -> {
                    SupplierResponseDTO mainSupplier = article.getMainSupplier();
                    return mainSupplier != null ? mainSupplier.getName() : "-";
                })
                .setHeader("Hauptlieferant")
                .setWidth("200px")
                .setFlexGrow(0)
                .setSortable(true)
                .setTooltipGenerator(article -> {
                    SupplierResponseDTO mainSupplier = article.getMainSupplier();
                    return mainSupplier != null ? mainSupplier.getName() : "-";
                });


        grid.addColumn(article -> {
                    String categories = article.getCategoryIds().stream()
                            .map(CategoryResponseDTO::getName)
                            .collect(Collectors.joining(", "));
                    return categories;
                })
                .setHeader("Kategorie")
                .setWidth("240px") // fixe Breite
                .setFlexGrow(0)
                .setSortable(true)
                .setTooltipGenerator(article -> article.getCategoryIds().stream()
                        .map(CategoryResponseDTO::getName)
                        .collect(Collectors.joining(", ")));

        grid.addComponentColumn(article -> {
                    Button infoButton = new Button(new Icon(VaadinIcon.ELLIPSIS_DOTS_H));
                    infoButton.getStyle().set("background", "transparent");
                    infoButton.getElement().setAttribute("title", "Weitere Artikelinfos anzeigen");
                    infoButton.addClickListener(e -> showArticleDetails(article));
                    return infoButton;
                })
                .setHeader("Weitere Infos")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.asSingleSelect().addValueChangeListener(event -> {
            ArticleResponseDTO selected = event.getValue();
        });
    }


    private void showArticleDetails(ArticleResponseDTO article) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Artikeldetails");

        Icon pfandIcon;
        if (Boolean.TRUE.equals(article.getHasDeposit())) {
            pfandIcon = VaadinIcon.CHECK.create();
            pfandIcon.getStyle().set("color", "#1C873B");
            pfandIcon.getElement().setAttribute("title", "Pfand");
        } else {
            pfandIcon = VaadinIcon.CLOSE_CIRCLE.create();
            pfandIcon.getStyle().set("color", "#C92525");
            pfandIcon.getElement().setAttribute("title", "Kein Pfand");
        }
        HorizontalLayout pfandLayout = new HorizontalLayout(
                new Span("Pfand:"),
                pfandIcon
        );
        pfandLayout.setAlignItems(Alignment.CENTER);

        // Lieferanten-Layout
        VerticalLayout supplierLayout = new VerticalLayout();
        supplierLayout.setPadding(false);
        supplierLayout.setSpacing(false);

        if (article.getSuppliers() != null && !article.getSuppliers().isEmpty()) {
            for (SupplierResponseDTO s : article.getSuppliers()) {
                Span supplierSpan = new Span(s.getName());
                if (article.getMainSupplier() != null && s.getId().equals(article.getMainSupplier().getId())) {
                    supplierSpan.getStyle().set("font-weight", "bold"); // Hauptlieferant fett
                }
                supplierLayout.add(supplierSpan);
            }
        } else {
            supplierLayout.add(new Span("-"));
        }


        String kategorieText = (article.getCategoryIds() == null || article.getCategoryIds().isEmpty())
                ? "-"
                : article.getCategoryIds().stream()
                .map(CategoryResponseDTO::getName)
                .collect(Collectors.joining(", "));

        VerticalLayout detailsLayout = new VerticalLayout(
                new Span("Artikelnummer (GTIN): " + safe(article.getArticleNumber())),
                new Span("Artikelname: " + safe(article.getName())),
                new Span("Lagerbestand: " + safe(article.getStockLevel())),
                new Span("EK-Preis (€): " + safe(article.getPurchasePrice())),
                new Span("VK-Preis (€): " + safe(article.getSellingPrice())),
                new Span("MwSt (%): " + safe(article.getTaxRatePercent())),
                new Span("Lieferant: " ), supplierLayout,
                new Span("Kategorie: " + kategorieText),
                new Span("Maße (H×B×T): " + String.format("%.1f x %.1f x %.1f cm",
                        article.getHeightCm(),
                        article.getWidthCm(),
                        article.getDepthCm())),
                new Span("Verfügbar: " + safe(Boolean.TRUE.equals(article.getAvailable()) ? "Ja" : "Nein")),
                pfandLayout,
                new Span("Beschreibung / Produktdetails: " + safe(article.getDescription()))
        );

        // Produktbild hinzufügen, wenn vorhanden
        if (article.getProductImage() != null && !article.getProductImage().isBlank()) {
            Image productImage = new Image(article.getProductImage(), "Produktbild");
            productImage.setWidth("200px");
            productImage.setHeight("auto");
            detailsLayout.addComponentAsFirst(productImage);
        }

        dialog.add(detailsLayout);
        dialog.getFooter().add(new Button("Schließen", e -> dialog.close()));
        dialog.open();
    }

    private void showDeleteArticleConfirmationDialog(ArticleResponseDTO article) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Löschen bestätigen");

        VerticalLayout content = new VerticalLayout(
                new Span("Möchten Sie den Artikel \"" + safe(article.getName()) + "\" wirklich endgültig löschen?"),
                new Span("Diese Aktion kann nicht rückgängig gemacht werden.")
        );
        content.setPadding(false);

        Button confirmButton = new Button("Löschen", event -> {
            try {
                articleService.deleteArticle(article.getId());
                confirmDialog.close();
                updateList();
                Notification.show("Artikel wurde erfolgreich gelöscht!", 3000, Notification.Position.BOTTOM_START);
            } catch (Exception ex) {
                confirmDialog.close();
                showErrorNotification("Fehler beim Löschen: " + ex.getMessage());
            }
        });
        confirmButton.getStyle().set("color", "white");
        confirmButton.getStyle().set("background-color", "#d32f2f");

        Button cancelButton = new Button("Abbrechen", event -> confirmDialog.close());

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        content.add(buttons);

        confirmDialog.add(content);
        confirmDialog.open();
    }

    private static TextField createSearchField(String label) {
        TextField tf = new TextField(label);
        tf.setClearButtonVisible(true);
        tf.setValueChangeMode(ValueChangeMode.LAZY);
        return tf;
    }

    private void configureSearchFields() {
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
                showDeleteArticleConfirmationDialog(selected);
            }
        });
        addToCartButton.addClickListener(e -> addSelectedToCart());

        manageCategoriesButton.addClickListener(e -> {
            CategoryManagement dlg =
                    new CategoryManagement(categoryService, this::refreshCategoriesInUi);
            dlg.open();
        });

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


        selectedArticles.clear();
        grid.getDataProvider().refreshAll();
    }

    private String safe(Object value) {
        return value == null ? "-" : value.toString();
    }

    private void refreshCategoriesInUi() {
        List<CategoryResponseDTO> all = categoryService.getAllCategories();
        all.sort((a, b) -> {
            String n1 = a.getName() != null ? a.getName() : "";
            String n2 = b.getName() != null ? b.getName() : "";
            return n1.compareToIgnoreCase(n2);
        });
        categoryCache.clear();
        categoryCache.addAll(all);
        categoryBox.setItems(categoryCache);
        categoryBox.setItemLabelGenerator(CategoryResponseDTO::getName);
        updateList();
    }

    java.util.function.Function<TextField, Double> parseDoubleOrNull = field -> {
        try {
            String v = field.getValue();
            if (v == null || v.trim().isEmpty()) {
                return null;
            }
            return Double.parseDouble(v.replace(",", ".").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    };

    private void showErrorNotification(String message) {
        Dialog notification = new Dialog();
        notification.setWidth("400px");

        VerticalLayout content = new VerticalLayout(new Span(message));
        content.setPadding(true);
        content.getStyle().set("color", "#c62828");
        content.getStyle().set("font-weight", "bold");

        Button closeButton = new Button("OK", e -> notification.close());
        closeButton.getStyle().set("background-color", "#f44336");
        closeButton.getStyle().set("color", "white");

        content.add(closeButton);
        notification.add(content);
        notification.open();
    }
}