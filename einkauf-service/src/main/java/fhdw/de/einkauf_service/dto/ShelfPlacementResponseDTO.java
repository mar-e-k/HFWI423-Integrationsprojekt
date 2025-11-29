package fhdw.de.einkauf_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShelfPlacementResponseDTO {

    private Long id;
    private Long shelfLevelId;
    private Long articleId;
    private String articleName;
    private Double positionX;
    private Double positionY;
    private Double widthCm;
    private Double heightCm;
    private LocalDateTime dateCreated;
}
