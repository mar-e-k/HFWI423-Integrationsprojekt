package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import fhdw.de.einkauf_service.dto.ShelfResponseDTO;
import fhdw.de.einkauf_service.dto.ShelfLevelResponseDTO;
import fhdw.de.einkauf_service.dto.ShelfPlacementResponseDTO;
import fhdw.de.einkauf_service.service.ShelfService;
import fhdw.de.einkauf_service.service.ShelfPlacementService;

import java.util.List;

/**
 * Detail view for a shelf showing:
 * • Shelf information (name, category, description, dimensions)
 * • All floors with details
 * • Articles placed on each floor
 */
@Route(value = "shelf/:id", layout = MainLayout.class)
public class ShelfDetailView extends VerticalLayout implements HasUrlParameter<String> {

    private final ShelfService shelfService;
    private final ShelfPlacementService placementService;

    private Long shelfId;
    private ShelfResponseDTO currentShelf;

    public ShelfDetailView(ShelfService shelfService,
                          ShelfPlacementService placementService) {
        this.shelfService = shelfService;
        this.placementService = placementService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setSpacing(true);
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            this.shelfId = Long.parseLong(parameter);
            this.currentShelf = shelfService.getShelf(shelfId);
            buildView();
        } catch (NumberFormatException e) {
            add(new Span("Ungültige Regal-ID"));
        } catch (Exception e) {
            add(new Span("Regal nicht gefunden: " + e.getMessage()));
        }
    }

    private void buildView() {
        removeAll();

        // Header with back button
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        RouterLink backLink = new RouterLink("← Zurück", ShelfManagementView.class);
        backLink.setHighlightCondition(com.vaadin.flow.router.HighlightConditions.sameLocation());

        header.add(backLink);
        add(header);

        // Title
        H2 title = new H2("Regal: " + currentShelf.getName());
        add(title);

        // Shelf information
        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setWidthFull();
        infoLayout.getStyle().set("border", "1px solid #ccc").set("padding", "15px").set("border-radius", "4px");

        infoLayout.add(new Span("Kategorie: " + currentShelf.getCategoryName()));
        infoLayout.add(new Span("Beschreibung: " + (currentShelf.getDescription() != null ? currentShelf.getDescription() : "Keine Beschreibung")));
        infoLayout.add(new Span("Größe: " + currentShelf.getWidthCm() + " cm (B) × " + currentShelf.getHeightCm() + " cm (H) × " + currentShelf.getDepthCm() + " cm (T)"));
        infoLayout.add(new Span("Anzahl Böden: " + (currentShelf.getLevels() != null ? currentShelf.getLevels().size() : 0)));

        add(infoLayout);

        // Floors and articles
        if (currentShelf.getLevels() != null && !currentShelf.getLevels().isEmpty()) {
            add(new H3("Böden und Artikel"));

            for (ShelfLevelResponseDTO level : currentShelf.getLevels()) {
                // Level section
                VerticalLayout levelLayout = new VerticalLayout();
                levelLayout.setWidthFull();
                levelLayout.getStyle()
                        .set("border-left", "4px solid #4CAF50")
                        .set("padding", "15px")
                        .set("background-color", "#f9f9f9")
                        .set("margin-bottom", "10px");

                Span levelTitle = new Span("Boden " + level.getLevelPosition());
                levelTitle.getStyle().set("font-weight", "bold").set("font-size", "16px");
                levelLayout.add(levelTitle);

                // Get placements for this level
                List<ShelfPlacementResponseDTO> placements = placementService.getPlacementsByShelfLevel(level.getId());

                if (placements.isEmpty()) {
                    levelLayout.add(new Span("Keine Artikel auf diesem Boden"));
                } else {
                    // Create grid for articles
                    Grid<ShelfPlacementResponseDTO> articleGrid = new Grid<>(ShelfPlacementResponseDTO.class);
                    articleGrid.setWidthFull();
                    articleGrid.setColumns();
                    articleGrid.addColumn(ShelfPlacementResponseDTO::getArticleName)
                            .setHeader("Artikel")
                            .setAutoWidth(true);
                    articleGrid.addColumn(p -> String.format("%.1f cm", p.getPositionX()))
                            .setHeader("X-Position")
                            .setAutoWidth(true);
                    articleGrid.addColumn(p -> String.format("%.1f cm", p.getPositionY()))
                            .setHeader("Y-Position")
                            .setAutoWidth(true);
                    articleGrid.addColumn(p -> String.format("%.1f × %.1f cm", p.getWidthCm(), p.getHeightCm()))
                            .setHeader("Abmessungen (B × H)")
                            .setAutoWidth(true);

                    articleGrid.setItems(placements);
                    levelLayout.add(articleGrid);
                }

                add(levelLayout);
            }
        } else {
            add(new Span("Dieses Regal hat noch keine Böden"));
        }

        // Action buttons
        HorizontalLayout actions = new HorizontalLayout();
        Button editButton = new Button("Bearbeiten", event -> {
            // Go back to shelves view to edit
            getUI().ifPresent(ui -> ui.navigate(ShelfManagementView.class));
        });
        actions.add(editButton);
        add(actions);
    }
}
