package fhdw.de.einkauf_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ShelfResponseDTO {

    public ShelfResponseDTO() {
    }

    public ShelfResponseDTO(Long id, String name, String description, Double widthCm, Double heightCm, Double depthCm, Long categoryId, String categoryName, List<ShelfLevelResponseDTO> levels, LocalDateTime dateCreated, LocalDateTime dateUpdated) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.widthCm = widthCm;
        this.heightCm = heightCm;
        this.depthCm = depthCm;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.levels = levels;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getWidthCm() {
        return widthCm;
    }

    public void setWidthCm(Double widthCm) {
        this.widthCm = widthCm;
    }

    public Double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Double heightCm) {
        this.heightCm = heightCm;
    }

    public Double getDepthCm() {
        return depthCm;
    }

    public void setDepthCm(Double depthCm) {
        this.depthCm = depthCm;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<ShelfLevelResponseDTO> getLevels() {
        return levels;
    }

    public void setLevels(List<ShelfLevelResponseDTO> levels) {
        this.levels = levels;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(LocalDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

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
