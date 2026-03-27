package fhdw.de.einkauf_service.dto;

import java.time.LocalDateTime;

public class ShelfLevelResponseDTO {
    public ShelfLevelResponseDTO(Long id, Integer levelPosition, Long shelfId, Integer placementCount, LocalDateTime dateCreated) {
        this.id = id;
        this.levelPosition = levelPosition;
        this.shelfId = shelfId;
        this.placementCount = placementCount;
        this.dateCreated = dateCreated;
    }

    public ShelfLevelResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLevelPosition() {
        return levelPosition;
    }

    public void setLevelPosition(Integer levelPosition) {
        this.levelPosition = levelPosition;
    }

    public Long getShelfId() {
        return shelfId;
    }

    public void setShelfId(Long shelfId) {
        this.shelfId = shelfId;
    }

    public Integer getPlacementCount() {
        return placementCount;
    }

    public void setPlacementCount(Integer placementCount) {
        this.placementCount = placementCount;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    private Long id;
    private Integer levelPosition;
    private Long shelfId;
    private Integer placementCount;
    private LocalDateTime dateCreated;
}
