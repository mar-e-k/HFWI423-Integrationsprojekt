package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import fhdw.de.einkauf_service.dto.*;
import fhdw.de.einkauf_service.service.ShelfService;
import fhdw.de.einkauf_service.service.ShelfPlacementService;
import fhdw.de.einkauf_service.service.ArticleService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Artikel-im-Regal-Platzierungs-Editor in Vaadin.
 * Bietet Funktionalität zum:
 * • Auswählen eines Regals und eines Bodens
 * • Visuelle Darstellung des Bodens mit platzierten Artikeln
 * • Hinzufügen neuer Artikelplatzierungen mit Überlappungsprüfung
 * • Bearbeiten und Löschen bestehender Platzierungen
 */
@Route(value = "shelf-placements", layout = MainLayout.class)
public class ShelfPlacementEditorView extends VerticalLayout {

    private final ShelfService shelfService;
    private final ShelfPlacementService placementService;
    private final ArticleService articleService;

    // Fixed shelf dimensions
    private static final Double SHELF_WIDTH = 100.0;    // cm
    private static final Double SHELF_HEIGHT = 150.0;   // cm

    // Selection controls
    private final ComboBox<ShelfResponseDTO> shelfCombo = new ComboBox<>("Regal auswählen");
    private final ComboBox<ShelfLevelResponseDTO> levelCombo = new ComboBox<>("Boden auswählen");

    // Visualization
    private final Div shelfVisualization = new Div();
    private final VerticalLayout placementsLayout = new VerticalLayout();

    // Placement management
    private final Grid<ShelfPlacementResponseDTO> placementsGrid = new Grid<>(ShelfPlacementResponseDTO.class);

    public ShelfPlacementEditorView(ShelfService shelfService,
                                   ShelfPlacementService placementService,
                                   ArticleService articleService) {
        this.shelfService = shelfService;
        this.placementService = placementService;
        this.articleService = articleService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        add(new H2("Artikel im Regal platzieren"));

        configureShelfCombo();
        configureLevelCombo();
        configureVisualization();
        configureGrid();

        // Selection layout
        HorizontalLayout selectionLayout = new HorizontalLayout(shelfCombo, levelCombo);
        selectionLayout.setAlignItems(Alignment.END);

        // Main content: visualization + grid
        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.setWidthFull();
        mainContent.setHeightFull();

        VerticalLayout visualizationPanel = new VerticalLayout(
                new H3("Visualisierung"),
                shelfVisualization
        );
        visualizationPanel.setWidthFull();
        visualizationPanel.setHeight("500px");

        VerticalLayout placementsPanel = new VerticalLayout(
                new H3("Platzierungen auf diesem Boden"),
                placementsGrid
        );
        placementsPanel.setWidthFull();
        placementsPanel.setFlexGrow(1);

        Button addPlacementButton = new Button("Artikel platzieren", e -> openPlacementDialog());

        add(selectionLayout, visualizationPanel, placementsPanel, addPlacementButton);
    }

    private void configureShelfCombo() {
        List<ShelfResponseDTO> shelves = shelfService.getAllShelves();
        shelfCombo.setItems(shelves);
        shelfCombo.setItemLabelGenerator(shelf ->
                shelf.getName() + " (" + shelf.getCategoryName() + ")"
        );
        shelfCombo.addValueChangeListener(event -> {
            levelCombo.clear();
            levelCombo.setItems(event.getValue() != null ? event.getValue().getLevels() : List.of());
            updateVisualization();
        });
    }

    private void configureLevelCombo() {
        levelCombo.setItemLabelGenerator(level -> "Boden " + level.getLevelPosition());
        levelCombo.addValueChangeListener(event -> updateVisualization());
    }

    private void configureVisualization() {
        shelfVisualization.setWidthFull();
        shelfVisualization.setHeight("400px");
        shelfVisualization.getStyle()
                .set("border", "2px solid #ccc")
                .set("position", "relative")
                .set("background-color", "#f9f9f9");
    }

    private void configureGrid() {
        placementsGrid.setSizeFull();
        placementsGrid.setColumns();
        placementsGrid.addColumn(ShelfPlacementResponseDTO::getArticleName).setHeader("Artikel").setAutoWidth(true);
        placementsGrid.addColumn(p -> String.format("X: %.1f cm", p.getPositionX())).setHeader("X-Position").setAutoWidth(true);
        placementsGrid.addColumn(p -> String.format("Y: %.1f cm", p.getPositionY())).setHeader("Y-Position").setAutoWidth(true);
        placementsGrid.addColumn(p -> String.format("%.1f × %.1f cm", p.getWidthCm(), p.getHeightCm()))
                .setHeader("Abmessungen (B × H)").setAutoWidth(true);

        placementsGrid.addComponentColumn(placement -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button editButton = new Button(new Icon(VaadinIcon.EDIT), e -> editPlacement(placement));
            editButton.setTooltipText("Bearbeiten");

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), e -> deletePlacement(placement));
            deleteButton.setTooltipText("Löschen");

            actions.add(editButton, deleteButton);
            return actions;
        }).setHeader("Aktionen").setAutoWidth(true);
    }

    private void updateVisualization() {
        shelfVisualization.removeAll();

        ShelfResponseDTO shelf = shelfCombo.getValue();
        ShelfLevelResponseDTO level = levelCombo.getValue();

        if (shelf == null || level == null) {
            shelfVisualization.add(new Span("Bitte wählen Sie ein Regal und einen Boden aus"));
            placementsGrid.setItems(List.of());
            return;
        }

        // Show placements for this level
        List<ShelfPlacementResponseDTO> placements = placementService.getPlacementsByShelfLevel(level.getId());
        placementsGrid.setItems(placements);

        // Create visual representation of shelf level
        Div levelVisual = new Div();
        levelVisual.setWidthFull();
        levelVisual.setHeight("400px");
        levelVisual.getStyle()
                .set("position", "relative")
                .set("border", "2px solid #333")
                .set("margin", "10px")
                .set("background", "linear-gradient(to right, #f5f5f5 0%, #ffffff 100%)")
                .set("box-shadow", "inset 0 2px 4px rgba(0,0,0,0.1)");

        // Draw scale reference (100cm width, 150cm height)
        Span widthLabel = new Span("100 cm");
        widthLabel.getStyle()
                .set("position", "absolute")
                .set("bottom", "-25px")
                .set("left", "50%")
                .set("transform", "translateX(-50%)")
                .set("font-size", "12px")
                .set("color", "#666");

        levelVisual.add(widthLabel);

        // Draw placements as rectangles with labels
        for (ShelfPlacementResponseDTO placement : placements) {
            Div placementBox = createPlacementBox(placement);
            levelVisual.add(placementBox);
        }

        shelfVisualization.add(levelVisual);
    }

    private Div createPlacementBox(ShelfPlacementResponseDTO placement) {
        Div box = new Div();

        // Calculate position and size as percentages of shelf dimensions
        double percentX = (placement.getPositionX() / SHELF_WIDTH) * 100;
        double percentY = (placement.getPositionY() / SHELF_HEIGHT) * 100;
        double percentWidth = (placement.getWidthCm() / SHELF_WIDTH) * 100;
        double percentHeight = (placement.getHeightCm() / SHELF_HEIGHT) * 100;

        box.getStyle()
                .set("position", "absolute")
                .set("left", percentX + "%")
                .set("bottom", percentY + "%")
                .set("width", percentWidth + "%")
                .set("height", percentHeight + "%")
                .set("background-color", "#4CAF50")
                .set("border", "1px solid #333")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("color", "white")
                .set("font-size", "12px")
                .set("font-weight", "bold")
                .set("cursor", "pointer")
                .set("padding", "4px")
                .set("box-sizing", "border-box")
                .set("overflow", "hidden")
                .set("white-space", "nowrap")
                .set("text-overflow", "ellipsis");

        box.setText(placement.getArticleName());
        box.setTitle(placement.getArticleName() + "\n" +
                String.format("Position: (%.1f, %.1f) cm\n", placement.getPositionX(), placement.getPositionY()) +
                String.format("Größe: %.1f × %.1f cm", placement.getWidthCm(), placement.getHeightCm()));

        box.addClickListener(e -> editPlacement(placement));

        return box;
    }

    private void openPlacementDialog() {
        ShelfResponseDTO shelf = shelfCombo.getValue();
        ShelfLevelResponseDTO level = levelCombo.getValue();

        if (shelf == null || level == null) {
            showError("Bitte wählen Sie ein Regal und einen Boden aus");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setHeaderTitle("Artikel platzieren");

        // Get articles from shelf's category
        ArticleFilterDTO filter = new ArticleFilterDTO();
        filter.setCategoryIds(List.of(shelf.getCategoryId()));
        List<ArticleResponseDTO> categoryArticles = articleService.findFilteredArticles(filter);

        // Create form - only article selection, all else is automatic
        ComboBox<ArticleResponseDTO> articleCombo = new ComboBox<>("Artikel");
        articleCombo.setItems(categoryArticles);
        articleCombo.setItemLabelGenerator(a -> a.getArticleNumber() + " - " + a.getName());
        articleCombo.setWidthFull();
        articleCombo.setRequired(true);

        // Display width and height (read-only, auto-populated from article)
        NumberField widthDisplay = new NumberField("Breite (cm)");
        widthDisplay.setWidthFull();
        widthDisplay.setReadOnly(true);

        NumberField heightDisplay = new NumberField("Höhe (cm)");
        heightDisplay.setWidthFull();
        heightDisplay.setReadOnly(true);

        Span positionInfo = new Span("Position wird automatisch berechnet");
        positionInfo.getStyle().set("color", "#666").set("font-size", "12px");

        // Update display when article is selected
        articleCombo.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                widthDisplay.setValue((double) event.getValue().getWidthCm());
                heightDisplay.setValue((double) event.getValue().getHeightCm());
            } else {
                widthDisplay.clear();
                heightDisplay.clear();
            }
        });

        VerticalLayout formLayout = new VerticalLayout(
                articleCombo,
                widthDisplay,
                heightDisplay,
                positionInfo
        );
        formLayout.setSpacing(true);

        // Buttons
        Button saveButton = new Button("Speichern", event -> {
            try {
                if (articleCombo.getValue() == null) {
                    showError("Bitte wählen Sie einen Artikel aus");
                    return;
                }

                ArticleResponseDTO article = articleCombo.getValue();

                // Calculate next available position automatically
                Double nextX = placementService.calculateNextAvailablePosition(level.getId(), article.getWidthCm());

                ShelfPlacementRequestDTO request = new ShelfPlacementRequestDTO();
                request.setShelfLevelId(level.getId());
                request.setArticleId(article.getId());
                request.setPositionX(nextX);
                request.setPositionY(0.0); // Always place at bottom for now
                request.setWidthCm(article.getWidthCm());
                request.setHeightCm(article.getHeightCm());

                placementService.createPlacement(request);
                dialog.close();
                updateVisualization();
            } catch (Exception ex) {
                showError("Fehler beim Platzieren: " + ex.getMessage());
            }
        });

        Button cancelButton = new Button("Abbrechen", event -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);

        dialog.add(formLayout, buttonLayout);
        dialog.open();
    }

    private void editPlacement(ShelfPlacementResponseDTO placement) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setHeaderTitle("Platzierung bearbeiten");

        // Display article info (read-only)
        Span articleInfo = new Span("Artikel: " + placement.getArticleName());
        articleInfo.getStyle().set("font-weight", "bold");

        NumberField widthDisplay = new NumberField("Breite (cm)");
        widthDisplay.setWidthFull();
        widthDisplay.setReadOnly(true);
        widthDisplay.setValue(placement.getWidthCm());

        NumberField heightDisplay = new NumberField("Höhe (cm)");
        heightDisplay.setWidthFull();
        heightDisplay.setReadOnly(true);
        heightDisplay.setValue(placement.getHeightCm());

        NumberField posXField = new NumberField("X-Position (cm)");
        posXField.setWidthFull();
        posXField.setRequired(true);
        posXField.setMin(0);
        posXField.setMax(SHELF_WIDTH);
        posXField.setValue(placement.getPositionX());

        NumberField posYField = new NumberField("Y-Position (cm)");
        posYField.setWidthFull();
        posYField.setRequired(true);
        posYField.setMin(0);
        posYField.setMax(SHELF_HEIGHT);
        posYField.setValue(placement.getPositionY());

        Span hint = new Span("Tipp: Verschieben Sie Artikel später per Drag & Drop (in Kürze verfügbar)");
        hint.getStyle().set("color", "#999").set("font-size", "12px");

        VerticalLayout formLayout = new VerticalLayout(
                articleInfo,
                widthDisplay,
                heightDisplay,
                posXField,
                posYField,
                hint
        );
        formLayout.setSpacing(true);

        Button saveButton = new Button("Speichern", event -> {
            try {
                ShelfPlacementRequestDTO request = new ShelfPlacementRequestDTO();
                request.setShelfLevelId(levelCombo.getValue().getId());
                request.setArticleId(placement.getArticleId());
                request.setPositionX(posXField.getValue());
                request.setPositionY(posYField.getValue());
                request.setWidthCm(placement.getWidthCm());
                request.setHeightCm(placement.getHeightCm());

                placementService.updatePlacement(placement.getId(), request);
                dialog.close();
                updateVisualization();
            } catch (Exception ex) {
                showError("Fehler beim Aktualisieren: " + ex.getMessage());
            }
        });

        Button cancelButton = new Button("Abbrechen", event -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);

        dialog.add(formLayout, buttonLayout);
        dialog.open();
    }

    private void deletePlacement(ShelfPlacementResponseDTO placement) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Platzierung löschen");

        Span message = new Span("Möchten Sie die Platzierung von '" + placement.getArticleName() +
                "' wirklich löschen?");

        Button confirmButton = new Button("Löschen", event -> {
            try {
                placementService.deletePlacement(placement.getId());
                confirmDialog.close();
                updateVisualization();
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
