package fhdw.de.einkauf_service.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShelfResponseDTO {

    private Long id;
    private String name;
    private String description;
    private Double widthCm;
    private Double heightCm;
    private Double depthCm;
    private Long categoryId;
    private String categoryName;
    private List<ShelfLevelResponseDTO> levels;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
}
