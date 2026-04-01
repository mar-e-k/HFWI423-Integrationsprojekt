package de.fhdw.vendix.commons.core.api.dto;

import java.math.BigDecimal;
import java.util.List;

public class ReceiptDTO extends AbstractDTO<Long> {

    private Long storeId;
    private Long registerId;
    private Long accountId;
    private BigDecimal totalAmount;
    private List<ReceiptLinkArticleDTO> receiptArticles;
    private Boolean isDepositOnly = false;
    private String depositRedemptionCode;

    public ReceiptDTO() {
        super();
    }

    public ReceiptDTO(Long id, Long storeId, Long registerId, Long accountId, BigDecimal totalAmount, List<ReceiptLinkArticleDTO> receiptArticles, Boolean isDepositOnly, String depositRedemptionCode) {
        super(id);
        this.storeId = storeId;
        this.registerId = registerId;
        this.accountId = accountId;
        this.totalAmount = totalAmount;
        this.receiptArticles = receiptArticles;
        this.isDepositOnly = isDepositOnly;
        this.depositRedemptionCode = depositRedemptionCode;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<ReceiptLinkArticleDTO> getReceiptArticles() {
        return receiptArticles;
    }

    public void setReceiptArticles(List<ReceiptLinkArticleDTO> receiptArticles) {
        this.receiptArticles = receiptArticles;
    }

    public Boolean isDepositOnly() {
        return isDepositOnly;
    }

    public void setDepositOnly(Boolean depositOnly) {
        isDepositOnly = depositOnly;
    }

    public String getDepositRedemptionCode() {
        return depositRedemptionCode;
    }

    public void setDepositRedemptionCode(String depositRedemptionCode) {
        this.depositRedemptionCode = depositRedemptionCode;
    }
}
