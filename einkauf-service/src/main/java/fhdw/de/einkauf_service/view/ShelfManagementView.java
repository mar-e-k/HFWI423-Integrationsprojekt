package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import fhdw.de.einkauf_service.dto.ShelfRequestDTO;
import fhdw.de.einkauf_service.dto.ShelfResponseDTO;
import fhdw.de.einkauf_service.dto.CategoryResponseDTO;
import fhdw.de.einkauf_service.dto.ShelfLevelResponseDTO;
import fhdw.de.einkauf_service.dto.ShelfPlacementResponseDTO;
import fhdw.de.einkauf_service.service.ShelfService;
import fhdw.de.einkauf_service.service.ArticleCategoryService;
import fhdw.de.einkauf_service.service.ShelfPlacementService;
import java.util.List;

/**
 * Regal-Verwaltungsansicht in Vaadin.
 * Bietet CRUD-Funktionalität für Regale mit:
 * • Grid-Ansicht aller Regale
 * • Filterung nach Name und Kategorie
 * • Formular zum Erstellen/Bearbeiten mit Kategorie-Auswahl
 * • Verwaltung von max. 5 Böden pro Regal
 * • Sicherheitswarnung beim Löschen
 */
@Route(value = "shelves", layout = MainLayout.class)
public class ShelfManagementView extends VerticalLayout {

    private final ShelfService shelfService;
    private final ArticleCategoryService categoryService;
    private final ShelfPlacementService placementService;

    private final Grid<ShelfResponseDTO> grid = new Grid<>(ShelfResponseDTO.class);

    // Search fields
    private final TextField nameField = createSearchField("Regal-Name");
    private final ComboBox<CategoryResponseDTO> categoryFilter = new ComboBox<>("Kategorie");
    private final Button clearButton = new Button("Suche abbrechen");

    // CRUD buttons
    private final Button addButton = new Button("Regal hinzufügen");
    private final Button editButton = new Button("Bearbeiten");
    private final Button deleteButton = new Button("Löschen");

    public ShelfManagementView(ShelfService shelfService,
                              ArticleCategoryService categoryService,
                              ShelfPlacementService placementService) {
        this.shelfService = shelfService;
        this.categoryService = categoryService;
        this.placementService = placementService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        add(new H2("Regal-Verwaltung"));

        configureGrid();
        configureCrudButtons();
        configureSearchFields();

        // Configure category filter
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        categoryFilter.setItems(categories);
        categoryFilter.setItemLabelGenerator(CategoryResponseDTO::getName);
        categoryFilter.addValueChangeListener(e -> updateList());

        HorizontalLayout searchLayout = new HorizontalLayout(
                nameField, categoryFilter, clearButton
        );
        searchLayout.setAlignItems(Alignment.END);

        HorizontalLayout crudButtons = new HorizontalLayout(addButton, editButton, deleteButton);

        add(searchLayout, crudButtons, grid);

        updateList();
    }

    private static TextField createSearchField(String label) {
        TextField tf = new TextField(label);
        tf.setClearButtonVisible(true);
        tf.setValueChangeMode(ValueChangeMode.EAGER);
        return tf;
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.setColumns();

        // Name column
        grid.addColumn(ShelfResponseDTO::getName).setHeader("Regal-Name").setAutoWidth(true);

        grid.addColumn(ShelfResponseDTO::getDescription).setHeader("Beschreibung").setAutoWidth(true);
        grid.addColumn(ShelfResponseDTO::getCategoryName).setHeader("Kategorie").setAutoWidth(true).setSortable(true);
        grid.addColumn(shelf -> shelf.getLevels() != null ? shelf.getLevels().size() : 0)
                .setHeader("Anzahl Böden")
                .setAutoWidth(true)
                .setSortable(true);

        // Details column with view button
        grid.addComponentColumn(shelf -> {
            Button viewButton = new Button(new Icon(VaadinIcon.EYE), event -> showShelfDetailDialog(shelf));
            viewButton.setTooltipText("Details anzeigen");
            return viewButton;
        }).setHeader("Details").setAutoWidth(true);

        grid.asSingleSelect().addValueChangeListener(event -> {
            boolean hasSelection = event.getValue() != null;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });
    }

    private void configureSearchFields() {
        nameField.addValueChangeListener(e -> updateList());
        clearButton.addClickListener(e -> {
            nameField.clear();
            categoryFilter.clear();
            updateList();
        });
    }

    private void configureCrudButtons() {
        addButton.addClickListener(e -> openShelfForm(null));

        editButton.addClickListener(e -> {
            ShelfResponseDTO selected = grid.asSingleSelect().getValue();
            if (selected != null) {
                openShelfForm(selected);
            }
        });

        deleteButton.addClickListener(e -> {
            ShelfResponseDTO selected = grid.asSingleSelect().getValue();
            if (selected != null) {
                showDeleteConfirmationDialog(selected);
            }
        });

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private void updateList() {
        String nameFilter = nameField.getValue();
        CategoryResponseDTO categoryFilter = this.categoryFilter.getValue();

        List<ShelfResponseDTO> shelves = shelfService.getAllShelves();

        // Filter by name
        if (nameFilter != null && !nameFilter.isEmpty()) {
            shelves = shelves.stream()
                    .filter(s -> s.getName() != null &&
                            s.getName().toLowerCase().contains(nameFilter.toLowerCase()))
                    .toList();
        }

        // Filter by category
        if (categoryFilter != null) {
            shelves = shelves.stream()
                    .filter(s -> s.getCategoryId() != null &&
                            s.getCategoryId().equals(categoryFilter.getId()))
                    .toList();
        }

        grid.setItems(shelves);
    }

    private void openShelfForm(ShelfResponseDTO shelf) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setHeaderTitle(shelf == null ? "Neues Regal hinzufügen" : "Regal bearbeiten");

        // Error message container inside dialog
        Span errorMessageSpan = new Span();
        errorMessageSpan.getStyle()
                .set("color", "var(--lumo-error-color)")
                .set("padding", "10px")
                .set("background-color", "var(--lumo-error-color-10pct)")
                .set("border-radius", "4px")
                .set("display", "none");

        // Shelf name
        TextField nameField = new TextField("Regal-Name");
        nameField.setWidthFull();
        nameField.setRequired(true);

        // Description
        TextField descriptionField = new TextField("Beschreibung");
        descriptionField.setWidthFull();

        // Category selection
        ComboBox<CategoryResponseDTO> categoryCombo = new ComboBox<>("Kategorie");
        categoryCombo.setWidthFull();
        categoryCombo.setRequired(true);
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        categoryCombo.setItems(categories);
        categoryCombo.setItemLabelGenerator(CategoryResponseDTO::getName);

        // Shelf info (read-only)
        Span shelfInfoSpan = new Span("Regalgröße: 100cm (B) × 150cm (H) × 47cm (T)");

        // Levels info (scrollable)
        VerticalLayout levelsLayout = new VerticalLayout();
        levelsLayout.setPadding(false);
        levelsLayout.setSpacing(false);

        // Declare refreshLevels first to allow forward reference from buttons
        Runnable[] refreshLevelsHolder = new Runnable[1];

        // Define refresh function
        refreshLevelsHolder[0] = () -> {
            if (shelf == null) return;
            levelsLayout.removeAll();
            ShelfResponseDTO updatedShelf = shelfService.getShelf(shelf.getId());

            if (updatedShelf.getLevels() != null && !updatedShelf.getLevels().isEmpty()) {
                Span levelsTitle = new Span("Vorhandene Böden: " + updatedShelf.getLevels().size());
                levelsLayout.add(levelsTitle);

                // Sort levels in descending order (highest at top, lowest at bottom)
                for (ShelfLevelResponseDTO level : updatedShelf.getLevels().stream()
                        .sorted((l1, l2) -> Integer.compare(l2.getLevelPosition(), l1.getLevelPosition()))
                        .toList()) {
                    HorizontalLayout levelLine = new HorizontalLayout();
                    levelLine.setWidthFull();
                    levelLine.setAlignItems(Alignment.CENTER);

                    Span levelInfo = new Span("Boden " + level.getLevelPosition() +
                            " - " + level.getPlacementCount() + " Artikel");

                    Button removeButton = new Button(new Icon(VaadinIcon.TRASH), event -> {
                        try {
                            shelfService.removeLevel(shelf.getId(), level.getLevelPosition());
                            refreshLevelsHolder[0].run();
                            showErrorInDialog(errorMessageSpan, null);
                        } catch (Exception ex) {
                            showErrorInDialog(errorMessageSpan, "Fehler beim Löschen des Bodens: " + ex.getMessage());
                        }
                    });
                    removeButton.setTooltipText("Boden entfernen");

                    levelLine.add(levelInfo, removeButton);
                    levelsLayout.add(levelLine);
                }
            }
        };

        if (shelf != null) {
            // Pre-fill form
            nameField.setValue(shelf.getName() != null ? shelf.getName() : "");
            descriptionField.setValue(shelf.getDescription() != null ? shelf.getDescription() : "");

            // Find and select the category
            categories.stream()
                    .filter(c -> c.getId().equals(shelf.getCategoryId()))
                    .findFirst()
                    .ifPresent(categoryCombo::setValue);

            // Disable category for edit (category is immutable)
            categoryCombo.setEnabled(false);

            // Show existing levels
            refreshLevelsHolder[0].run();
        }

        // Add level button (max 5 levels)
        Button addLevelButton = new Button("Boden hinzufügen", event -> {
            try {
                if (shelf == null) {
                    showErrorInDialog(errorMessageSpan, "Bitte speichern Sie das Regal zuerst");
                    return;
                }

                ShelfResponseDTO currentShelf = shelfService.getShelf(shelf.getId());
                if (currentShelf.getLevels() != null && currentShelf.getLevels().size() >= 5) {
                    showErrorInDialog(errorMessageSpan, "Ein Regal kann maximal 5 Böden haben");
                    return;
                }

                // Find first available position between 1 and 5
                int nextPosition = 1;
                if (currentShelf.getLevels() != null && !currentShelf.getLevels().isEmpty()) {
                    java.util.Set<Integer> usedPositions = currentShelf.getLevels().stream()
                            .map(ShelfLevelResponseDTO::getLevelPosition)
                            .collect(java.util.stream.Collectors.toSet());

                    for (int i = 1; i <= 5; i++) {
                        if (!usedPositions.contains(i)) {
                            nextPosition = i;
                            break;
                        }
                    }
                }
                shelfService.addLevel(shelf.getId(), nextPosition);
                refreshLevelsHolder[0].run();
                showErrorInDialog(errorMessageSpan, null);
            } catch (Exception ex) {
                showErrorInDialog(errorMessageSpan, "Fehler beim Hinzufügen des Bodens: " + ex.getMessage());
            }
        });

        VerticalLayout formLayout = new VerticalLayout(
                errorMessageSpan,
                nameField,
                descriptionField,
                categoryCombo,
                shelfInfoSpan,
                levelsLayout,
                addLevelButton
        );
        formLayout.setSpacing(true);

        // Buttons
        Button saveButton = new Button("Speichern", event -> {
            try {
                ShelfRequestDTO request = new ShelfRequestDTO();
                request.setName(nameField.getValue());
                request.setDescription(descriptionField.getValue());
                request.setCategoryId(categoryCombo.getValue() != null ? categoryCombo.getValue().getId() : null);

                if (shelf == null) {
                    shelfService.createShelf(request);
                } else {
                    shelfService.updateShelf(shelf.getId(), request);
                }

                dialog.close();
                updateList();
            } catch (Exception ex) {
                showErrorInDialog(errorMessageSpan, "Fehler beim Speichern: " + ex.getMessage());
            }
        });

        Button cancelButton = new Button("Abbrechen", event -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);

        dialog.add(formLayout, buttonLayout);
        dialog.open();
    }

    private void showErrorInDialog(Span errorMessageSpan, String message) {
        if (message == null || message.isEmpty()) {
            errorMessageSpan.getStyle().set("display", "none");
        } else {
            errorMessageSpan.setText(message);
            errorMessageSpan.getStyle().set("display", "block");
        }
    }

    private void showDeleteConfirmationDialog(ShelfResponseDTO shelf) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Regal löschen");

        Span message = new Span("Möchten Sie das Regal '" + shelf.getName() +
                "' wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.");
        message.setWidthFull();

        Button confirmButton = new Button("Löschen", event -> {
            try {
                shelfService.deleteShelf(shelf.getId());
                confirmDialog.close();
                grid.asSingleSelect().clear();
                updateList();
            } catch (Exception ex) {
                showError("Fehler beim Löschen: " + ex.getMessage());
            }
        });
        confirmButton.setThemeName("error");

        Button cancelButton = new Button("Abbrechen", event -> confirmDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(confirmButton, cancelButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);

        confirmDialog.add(message, buttonLayout);
        confirmDialog.open();
    }

    private void showShelfDetailDialog(ShelfResponseDTO shelf) {
        Dialog dialog = new Dialog();
        dialog.setWidth("95%");
        dialog.setMaxHeight("95vh");
        dialog.setHeaderTitle("Regal: " + shelf.getName());

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);

        // Print button at top
        Button printButton = new Button("Drucken", e -> {
            com.vaadin.flow.component.UI.getCurrent().getPage().executeJs(
                "window.print();"
            );
        });
        printButton.setThemeName("primary");
        content.add(printButton);

        // Display each floor with visualization (printable view)
        // Sort floors in descending order: highest floor (5) at top, lowest floor (1) at bottom
        if (shelf.getLevels() != null && !shelf.getLevels().isEmpty()) {
            var sortedLevels = shelf.getLevels().stream()
                    .sorted((l1, l2) -> Integer.compare(l2.getLevelPosition(), l1.getLevelPosition()))
                    .toList();

            for (ShelfLevelResponseDTO level : sortedLevels) {
                // Floor title
                H3 floorTitle = new H3("Boden " + level.getLevelPosition());
                content.add(floorTitle);

                // Create visualization for this floor
                Div levelVisual = createFloorVisualization(level);
                content.add(levelVisual);
            }
        } else {
            content.add(new Span("Dieses Regal hat noch keine Böden"));
        }

        dialog.add(content);
        dialog.open();
    }

    private Div createFloorVisualization(ShelfLevelResponseDTO level) {
        Div levelVisual = new Div();
        // Scaled to ~80% for A4 preview (1200px instead of 1620px)
        levelVisual.setWidth("1200px");
        levelVisual.setHeight("375px");
        levelVisual.getStyle()
                .set("position", "relative")
                .set("border", "2px solid #333")
                .set("margin", "10px 0")
                .set("background", "linear-gradient(to right, #f5f5f5 0%, #ffffff 100%)")
                .set("box-shadow", "inset 0 2px 4px rgba(0,0,0,0.1)")
                .set("overflow", "visible");

        // Get placements for this level
        var placements = placementService.getPlacementsByShelfLevel(level.getId());

        // Draw placements as product images
        for (ShelfPlacementResponseDTO placement : placements) {
            Div container = createPlacementBox(placement);
            levelVisual.add(container);
        }

        // Draw scale reference
        Span widthLabel = new Span("100 cm");
        widthLabel.getStyle()
                .set("position", "absolute")
                .set("bottom", "-25px")
                .set("left", "50%")
                .set("transform", "translateX(-50%)")
                .set("font-size", "12px")
                .set("color", "#666");
        levelVisual.add(widthLabel);

        return levelVisual;
    }

    private Div createPlacementBox(ShelfPlacementResponseDTO placement) {
        Div container = new Div();

        // Calculate position and size as percentages of shelf dimensions (100cm wide, 150cm tall)
        double percentX = (placement.getPositionX() / 100.0) * 100;
        double percentY = (placement.getPositionY() / 150.0) * 100;
        double percentWidth = (placement.getWidthCm() / 100.0) * 100;
        double percentHeight = (placement.getHeightCm() / 150.0) * 100;

        container.getStyle()
                .set("position", "absolute")
                .set("left", percentX + "%")
                .set("bottom", percentY + "%")
                .set("width", percentWidth + "%")
                .set("height", percentHeight + "%")
                .set("border", "2px solid #999")
                .set("background-color", "white")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("cursor", "pointer")
                .set("box-sizing", "border-box")
                .set("overflow", "hidden")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.2)");

        // Display product image if available, otherwise show placeholder
        if (placement.getProductImage() != null && !placement.getProductImage().isEmpty()) {
            Image productImage = new Image(placement.getProductImage(), placement.getArticleName());
            productImage.setWidth("100%");
            productImage.setHeight("100%");
            productImage.getElement().getStyle()
                    .set("object-fit", "contain")
                    .set("object-position", "center");
            container.add(productImage);
        } else {
            // Fallback: show "Kein Bild" if no image available
            Span fallback = new Span("Kein Bild");
            fallback.getStyle()
                    .set("text-align", "center")
                    .set("padding", "8px")
                    .set("font-weight", "bold")
                    .set("color", "#999")
                    .set("font-size", "14px");
            container.add(fallback);
        }

        return container;
    }

    private void showError(String message) {
        Span errorSpan = new Span(message);
        errorSpan.getStyle().set("color", "var(--lumo-error-color)");
        add(errorSpan);
    }
}
