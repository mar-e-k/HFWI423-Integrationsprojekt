package de.fhdw.commons.api.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("LogisticMessage")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "__type")
public class LogisticMessageDTO {

    private Long storeId;
    private Long articleId;
    private Long quantity;
    private Boolean isBelowMinimumStockLevel;

    public LogisticMessageDTO() {
        super();
    }

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
}