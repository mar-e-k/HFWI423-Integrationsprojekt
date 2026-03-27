package fhdw.de.einkauf_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ShelfPlacementRequestDTO {

    public ShelfPlacementRequestDTO() {
    }

    public ShelfPlacementRequestDTO(Long shelfLevelId, Long articleId, Double positionX, Double positionY, Double widthCm, Double heightCm) {
        this.shelfLevelId = shelfLevelId;
        this.articleId = articleId;
        this.positionX = positionX;
        this.positionY = positionY;
        this.widthCm = widthCm;
        this.heightCm = heightCm;
    }

    public Long getShelfLevelId() {
        return shelfLevelId;
    }

    public void setShelfLevelId(Long shelfLevelId) {
        this.shelfLevelId = shelfLevelId;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public Double getPositionX() {
        return positionX;
    }

    public void setPositionX(Double positionX) {
        this.positionX = positionX;
    }

    public Double getPositionY() {
        return positionY;
    }

    public void setPositionY(Double positionY) {
        this.positionY = positionY;
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
