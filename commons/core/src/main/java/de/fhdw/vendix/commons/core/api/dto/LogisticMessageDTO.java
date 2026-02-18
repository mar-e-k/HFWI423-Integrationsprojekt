package de.fhdw.vendix.commons.core.api.dto;

public class LogisticMessageDTO implements GenericDTO<Long> {

    private Long storeId;
    private Long articleId;
    private Long quantity;
    private Boolean isBelowMinimumStockLevel;

    public LogisticMessageDTO() {}

    public LogisticMessageDTO(Long storeId, Long articleId, Long quantity, boolean isBelowMinimumStockLevel) {
        this.storeId = storeId;
        this.articleId = articleId;
        this.quantity = quantity;
        this.isBelowMinimumStockLevel = isBelowMinimumStockLevel;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public boolean isBelowMinimumStockLevel() {
        return isBelowMinimumStockLevel;
    }

    public void setBelowMinimumStockLevel(boolean belowMinimumStockLevel) {
        isBelowMinimumStockLevel = belowMinimumStockLevel;
    }

    @Override
    @Deprecated
    public Long getId() {
        return null;
    }

    @Override
    @Deprecated
    public void setId(Long id) {}
}