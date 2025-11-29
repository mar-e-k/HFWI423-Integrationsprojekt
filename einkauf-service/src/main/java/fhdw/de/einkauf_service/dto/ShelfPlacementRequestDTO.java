package fhdw.de.einkauf_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShelfPlacementRequestDTO {

    @NotNull(message = "Regal-Level-ID ist erforderlich")
    private Long shelfLevelId;

    @NotNull(message = "Artikel-ID ist erforderlich")
    private Long articleId;

    @NotNull(message = "X-Position ist erforderlich")
    @Min(value = 0, message = "X-Position darf nicht negativ sein")
    private Double positionX;

    @NotNull(message = "Y-Position ist erforderlich")
    @Min(value = 0, message = "Y-Position darf nicht negativ sein")
    private Double positionY;

    @NotNull(message = "Breite ist erforderlich")
    @Min(value = 0, message = "Breite darf nicht negativ sein")
    private Double widthCm;

    @NotNull(message = "Höhe ist erforderlich")
    @Min(value = 0, message = "Höhe darf nicht negativ sein")
    private Double heightCm;
}
