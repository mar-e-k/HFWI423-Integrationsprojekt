package de.fhdw.commons.api.dto;

import java.math.BigDecimal;

public class ReceiptLinkArticleDTO extends AbstractDTO<Long>{

    private Long receiptId;
    private Long articleId;
    private BigDecimal price;
    private Integer amount;
    private BigDecimal taxRate;
    private BigDecimal overridePrice;
    private OverrideReasonEnum overrideReason;
    private BigDecimal discountedByPercent;
    private DepositStatus depositStatus;

    public ReceiptLinkArticleDTO() {
        super();
    }

    public ReceiptLinkArticleDTO(Long id, Long receiptId, Long articleId, BigDecimal price, Integer amount, BigDecimal taxRate, BigDecimal overridePrice, OverrideReasonEnum overrideReason, BigDecimal discountedByPercent, DepositStatus depositStatus) {
        super(id);
        this.receiptId = receiptId;
        this.articleId = articleId;
        this.price = price;
        this.amount = amount;
        this.taxRate = taxRate;
        this.overridePrice = overridePrice;
        this.overrideReason = overrideReason;
        this.discountedByPercent = discountedByPercent;
        this.depositStatus = depositStatus;
    }

    public Long getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(Long receiptId) {
        this.receiptId = receiptId;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getOverridePrice() {
        return overridePrice;
    }

    public void setOverridePrice(BigDecimal overridePrice) {
        this.overridePrice = overridePrice;
    }

    public OverrideReasonEnum getOverrideReason() {
        return overrideReason;
    }

    public void setOverrideReason(OverrideReasonEnum overrideReason) {
        this.overrideReason = overrideReason;
    }

    public BigDecimal getDiscountedByPercent() {
        return discountedByPercent;
    }

    public void setDiscountedByPercent(BigDecimal discountedByPercent) {
        this.discountedByPercent = discountedByPercent;
    }

    public DepositStatus getDepositStatus() {
        return depositStatus;
    }

    public void setDepositStatus(DepositStatus depositStatus) {
        this.depositStatus = depositStatus;
    }
}
