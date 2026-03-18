package de.fhdw.vendix.store.core.persistance.receipt_line;

import de.fhdw.vendix.commons.api.domain.receipt_line.dto.OverrideReasonEnum;
import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.persistance.receipt.Receipt;
import de.fhdw.vendix.store.core.persistance.article.Article;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
public class ReceiptLine extends AbstractSpringDataAuditingEntity<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Receipt receipt;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Article article;

    @Column(nullable = false)
    @Min(1)
    private long amount;

    //TODO: make embeddable

    @DecimalMin(value = "0.00")
    private BigDecimal overriddenPrice;

    @Enumerated(EnumType.STRING)
    private OverrideReasonEnum overriddenPriceReason;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal overriddenDiscount;

    @Enumerated(EnumType.STRING)
    private OverrideReasonEnum overrideDiscountReason;

    protected ReceiptLine() {}

    protected ReceiptLine(Receipt receipt, Article article, long amount) {
        this.receipt = receipt;
        this.article = article;
        this.amount = amount;
    }

    protected ReceiptLine(Long id, Receipt receipt, Article article, long amount) {
        super(id);
        this.receipt = receipt;
        this.article = article;
        this.amount = amount;
    }

    protected ReceiptLine(
            Receipt receipt,
            Article article,
            long amount,
            BigDecimal overriddenPrice,
            OverrideReasonEnum overriddenPriceReason,
            BigDecimal overriddenDiscount,
            OverrideReasonEnum overrideDiscountReason
    ) {
        this.receipt = receipt;
        this.article = article;
        this.amount = amount;
        this.overriddenPrice = overriddenPrice;
        this.overriddenPriceReason = overriddenPriceReason;
        this.overriddenDiscount = overriddenDiscount;
        this.overrideDiscountReason = overrideDiscountReason;
    }

    public ReceiptLine(
            Long id,
            Receipt receipt,
            Article article,
            long amount,
            BigDecimal overriddenPrice,
            OverrideReasonEnum overriddenPriceReason,
            BigDecimal overriddenDiscount,
            OverrideReasonEnum overrideDiscountReason
    ) {
        super(id);
        this.receipt = receipt;
        this.article = article;
        this.amount = amount;
        this.overriddenPrice = overriddenPrice;
        this.overriddenPriceReason = overriddenPriceReason;
        this.overriddenDiscount = overriddenDiscount;
        this.overrideDiscountReason = overrideDiscountReason;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public Article getArticle() {
        return article;
    }

    public long getAmount() {
        return amount;
    }

    public BigDecimal getOverriddenPrice() {
        return overriddenPrice;
    }

    public OverrideReasonEnum getOverriddenPriceReason() {
        return overriddenPriceReason;
    }

    public BigDecimal getOverriddenDiscount() {
        return overriddenDiscount;
    }

    public OverrideReasonEnum getOverrideDiscountReason() {
        return overrideDiscountReason;
    }

    public void overridePrice(BigDecimal overridePrice, OverrideReasonEnum overridePriceReason) {
        this.overriddenPrice = overridePrice;
        this.overriddenPriceReason = overridePriceReason;
    }

    public void overrideDiscount(BigDecimal overrideDiscount, OverrideReasonEnum overrideDiscountReason) {
        this.overriddenDiscount = overrideDiscount;
        this.overrideDiscountReason = overrideDiscountReason;
    }
}
