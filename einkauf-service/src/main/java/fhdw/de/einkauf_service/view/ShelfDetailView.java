package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
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

        setSizeFull();
        setSpacing(false);
        setPadding(false);

        // Title
        H2 title = new H2("Regal: " + currentShelf.getName());
        add(title);

        // Display each floor with its visualization
        if (currentShelf.getLevels() != null && !currentShelf.getLevels().isEmpty()) {
            for (ShelfLevelResponseDTO level : currentShelf.getLevels()) {
                // Floor title
                H3 floorTitle = new H3("Boden " + level.getLevelPosition());
                add(floorTitle);

                // Create visualization for this floor (same as in ShelfPlacementEditorView)
                Div levelVisual = createFloorVisualization(level);
                add(levelVisual);
            }
        } else {
            add(new Span("Dieses Regal hat noch keine Böden"));
        }
    }

    private Div createFloorVisualization(ShelfLevelResponseDTO level) {
        Div levelVisual = new Div();
        levelVisual.setWidth("1620px");
        levelVisual.setHeight("500px");
        levelVisual.getStyle()
                .set("position", "relative")
                .set("border", "2px solid #333")
                .set("margin", "10px 0")
                .set("background", "linear-gradient(to right, #f5f5f5 0%, #ffffff 100%)")
                .set("box-shadow", "inset 0 2px 4px rgba(0,0,0,0.1)")
                .set("overflow", "visible");

        // Get placements for this level
        List<ShelfPlacementResponseDTO> placements = placementService.getPlacementsByShelfLevel(level.getId());

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
        double percentX = (placement.getPositionX() / 100.0) * 100;  // 100cm shelf width
        double percentY = (placement.getPositionY() / 150.0) * 100;  // 150cm shelf height
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
            com.vaadin.flow.component.html.Image productImage =
                new com.vaadin.flow.component.html.Image(placement.getProductImage(), placement.getArticleName());
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
}
