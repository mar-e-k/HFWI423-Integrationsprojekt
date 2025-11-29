package fhdw.de.einkauf_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShelfLevelResponseDTO {

    private Long id;
    private Integer levelPosition;
    private Long shelfId;
    private Integer placementCount;
    private LocalDateTime dateCreated;
}
