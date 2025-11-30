package de.fhdw.commons.api.dto;

import java.util.List;

public class ReceiptDTO extends AbstractDTO<Long> {

    private Long storeId;
    private Long registerId;
    private Long accountId;
    private List<ReceiptLinkArticleDTO> receiptArticles;

    public ReceiptDTO() {
        super();
    }

    public ReceiptDTO(Long id, Long storeId, Long registerId, Long accountId, List<ReceiptLinkArticleDTO> receiptArticles) {
        super(id);
        this.storeId = storeId;
        this.registerId = registerId;
        this.accountId = accountId;
        this.receiptArticles = receiptArticles;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getRegisterId() {
        return registerId;
    }

    public void setRegisterId(Long registerId) {
        this.registerId = registerId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public List<ReceiptLinkArticleDTO> getReceiptArticles() {
        return receiptArticles;
    }

    public void setReceiptArticles(List<ReceiptLinkArticleDTO> receiptArticles) {
        this.receiptArticles = receiptArticles;
    }
}