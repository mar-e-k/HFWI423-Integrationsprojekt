package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import fhdw.de.einkauf_service.dto.CategoryRequestDTO;
import fhdw.de.einkauf_service.dto.CategoryResponseDTO;
import fhdw.de.einkauf_service.service.ArticleCategoryService;

import java.util.List;

public class CategoryManagement extends Dialog {

    private final ArticleCategoryService categoryService;
    private final Runnable onCategoriesChanged;

    private final Grid<CategoryResponseDTO> categoryGrid =
            new Grid<>(CategoryResponseDTO.class, false);

    private final TextField nameField = new TextField("Name");
    private final TextField descriptionField = new TextField("Beschreibung");

    private final Button newButton = new Button("Neu anlegen");
    private final Button saveButton = new Button("Änderung speichern");
    private final Button deleteButton = new Button("Löschen");
    private final Button closeButton = new Button("Schließen", e -> close());

    private CategoryResponseDTO selectedCategory;

    public CategoryManagement(ArticleCategoryService categoryService,
                                    Runnable onCategoriesChanged) {
        this.categoryService = categoryService;
        this.onCategoriesChanged = onCategoriesChanged;

        setHeaderTitle("Kategorien verwalten");

        configureGrid();
        configureButtons();

        nameField.setRequired(true);
        nameField.setRequiredIndicatorVisible(true);

        HorizontalLayout formButtons = new HorizontalLayout(newButton, saveButton, deleteButton);
        VerticalLayout content = new VerticalLayout(categoryGrid, nameField, descriptionField, formButtons);
        content.setSizeFull();

        add(content);
        getFooter().add(closeButton);

        setWidth("800px");
        setHeight("600px");

        refreshGrid();
        resetSelectionAndButtons();
    }

    private void configureGrid() {
        categoryGrid.addColumn(CategoryResponseDTO::getName)
                .setHeader("Name")
                .setAutoWidth(true);
        categoryGrid.addColumn(CategoryResponseDTO::getDescription)
                .setHeader("Beschreibung")
                .setAutoWidth(true);

        categoryGrid.asSingleSelect().addValueChangeListener(event -> {
            selectedCategory = event.getValue();
            if (selectedCategory != null) {
                nameField.setValue(selectedCategory.getName() != null ? selectedCategory.getName() : "");
                descriptionField.setValue(selectedCategory.getDescription() != null ? selectedCategory.getDescription() : "");
            } else {
                nameField.clear();
                descriptionField.clear();
            }
            updateButtonStates();
        });
    }

    private void configureButtons() {
        newButton.addClickListener(e -> {
            if (nameField.isEmpty()) {
                Notification.show("Name darf nicht leer sein");
                return;
            }

            CategoryRequestDTO req = new CategoryRequestDTO();
            req.setName(nameField.getValue());
            req.setDescription(descriptionField.getValue());

            try {
                categoryService.createCategory(req);
                if (onCategoriesChanged != null) {
                    onCategoriesChanged.run();
                }
                refreshGrid();
                resetAfterChange();   // STATT dupliziertem Code
            } catch (IllegalArgumentException ex) {
                Notification.show("Kategorie mit diesem Namen existiert bereits.", 4000, Notification.Position.BOTTOM_START);
            }
        });

        saveButton.addClickListener(e -> {
            if (selectedCategory == null) {
                return;
            }
            if (nameField.isEmpty()) {
                Notification.show("Name darf nicht leer sein");
                return;
            }

            CategoryRequestDTO req = new CategoryRequestDTO();
            req.setName(nameField.getValue());
            req.setDescription(descriptionField.getValue());

            categoryService.updateCategory(selectedCategory.getId(), req);
            if (onCategoriesChanged != null) {
                onCategoriesChanged.run();
            }
            refreshGrid();
            resetAfterChange();       // HIER
        });

        deleteButton.addClickListener(e -> {
            if (selectedCategory == null) {
                return;
            }
            try {
                categoryService.deleteCategory(selectedCategory.getId());
                if (onCategoriesChanged != null) {
                    onCategoriesChanged.run();
                }
                refreshGrid();
                resetAfterChange();
            } catch (Exception ex) {
                Notification.show(
                        "Diese Kategorie wird noch von Artikeln verwendet und kann nicht gelöscht werden.");
            }
        });

        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedCategory != null;
        saveButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
        newButton.setEnabled(!hasSelection);
    }

    private void refreshGrid() {
        List<CategoryResponseDTO> all = categoryService.getAllCategories();
        categoryGrid.setItems(all);
    }

    private void resetSelectionAndButtons() {
        selectedCategory = null;
        categoryGrid.deselectAll();
        nameField.clear();
        descriptionField.clear();
        updateButtonStates();
    }

    private void resetAfterChange() {
        selectedCategory = null;
        categoryGrid.deselectAll();
        nameField.clear();
        nameField.setInvalid(false);
        descriptionField.clear();
        updateButtonStates();
    }
}