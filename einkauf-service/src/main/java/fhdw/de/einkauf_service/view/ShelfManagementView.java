package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
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
import fhdw.de.einkauf_service.service.ShelfService;
import fhdw.de.einkauf_service.service.ArticleCategoryService;
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
                              ArticleCategoryService categoryService) {
        this.shelfService = shelfService;
        this.categoryService = categoryService;

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
        grid.addColumn(ShelfResponseDTO::getName).setHeader("Regal-Name").setAutoWidth(true).setSortable(true);
        grid.addColumn(ShelfResponseDTO::getDescription).setHeader("Beschreibung").setAutoWidth(true);
        grid.addColumn(ShelfResponseDTO::getCategoryName).setHeader("Kategorie").setAutoWidth(true).setSortable(true);
        grid.addColumn(shelf -> shelf.getLevels() != null ? shelf.getLevels().size() : 0)
                .setHeader("Anzahl Böden")
                .setAutoWidth(true)
                .setSortable(true);

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

        // Function to refresh levels display
        Runnable refreshLevels = () -> {
            levelsLayout.removeAll();
            ShelfResponseDTO updatedShelf = shelfService.getShelf(shelf.getId());

            if (updatedShelf.getLevels() != null && !updatedShelf.getLevels().isEmpty()) {
                Span levelsTitle = new Span("Vorhandene Böden: " + updatedShelf.getLevels().size());
                levelsLayout.add(levelsTitle);

                for (ShelfLevelResponseDTO level : updatedShelf.getLevels()) {
                    HorizontalLayout levelLine = new HorizontalLayout();
                    levelLine.setWidthFull();
                    levelLine.setAlignItems(Alignment.CENTER);

                    Span levelInfo = new Span("Boden " + level.getLevelPosition() +
                            " - " + level.getPlacementCount() + " Artikel");

                    Button removeButton = new Button(new Icon(VaadinIcon.TRASH), event -> {
                        try {
                            shelfService.removeLevel(shelf.getId(), level.getLevelPosition());
                            refreshLevels.run();
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
            refreshLevels.run();
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

                // Find next available position (max position + 1)
                int nextPosition = 1;
                if (currentShelf.getLevels() != null && !currentShelf.getLevels().isEmpty()) {
                    nextPosition = currentShelf.getLevels().stream()
                            .mapToInt(ShelfLevelResponseDTO::getLevelPosition)
                            .max()
                            .orElse(0) + 1;
                }
                shelfService.addLevel(shelf.getId(), nextPosition);
                refreshLevels.run();
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

    private void showError(String message) {
        Span errorSpan = new Span(message);
        errorSpan.getStyle().set("color", "var(--lumo-error-color)");
        add(errorSpan);
    }
}
