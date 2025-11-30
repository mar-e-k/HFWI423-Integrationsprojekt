package de.fhdw.commons.api.dto;

public class LogisticMessageDTO {

    private Long storeId;
    private Long articleId;
    private Long quantity;

    public LogisticMessageDTO() {
        super();
    }

    public LogisticMessageDTO(Long storeId, Long articleId, Long quantity) {
        this.storeId = storeId;
        this.articleId = articleId;
        this.quantity = quantity;
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
}