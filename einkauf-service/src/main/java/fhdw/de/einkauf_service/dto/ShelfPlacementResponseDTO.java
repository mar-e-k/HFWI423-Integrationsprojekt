package fhdw.de.einkauf_service.dto;

import java.time.LocalDateTime;

public class ShelfPlacementResponseDTO {

    public ShelfPlacementResponseDTO() {
    }

    public ShelfPlacementResponseDTO(Long id, Long shelfLevelId, Long articleId, String articleName, String productImage, Double positionX, Double positionY, Double widthCm, Double heightCm, LocalDateTime dateCreated) {
        this.id = id;
        this.shelfLevelId = shelfLevelId;
        this.articleId = articleId;
        this.articleName = articleName;
        this.productImage = productImage;
        this.positionX = positionX;
        this.positionY = positionY;
        this.widthCm = widthCm;
        this.heightCm = heightCm;
        this.dateCreated = dateCreated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getArticleName() {
        return articleName;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
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

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    private Long id;
    private Long shelfLevelId;
    private Long articleId;
    private String articleName;
    private String productImage;
    private Double positionX;
    private Double positionY;
    private Double widthCm;
    private Double heightCm;
    private LocalDateTime dateCreated;
}
