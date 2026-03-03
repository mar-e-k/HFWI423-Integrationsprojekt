package de.fhdw.vendix.store.core.domain.receipt_line;

import de.fhdw.vendix.commons.api.domain.receipt_line.dto.OverrideReasonEnum;
import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.domain.receipt.Receipt;
import de.fhdw.vendix.store.core.domain.article.Article;
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

    @DecimalMin(value = "0.00")
    private BigDecimal overridePrice;

    @Enumerated(EnumType.STRING)
    private OverrideReasonEnum overridePriceReason;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal overriddenDiscount;

    @Enumerated(EnumType.STRING)
    private OverrideReasonEnum overrideDiscountReason;

    protected ReceiptLine() {}

    public ReceiptLine(Receipt receipt, Article article, long amount) {
        this.receipt = receipt;
        this.article = article;
        this.amount = amount;
    }

    public ReceiptLine(Receipt receipt, Article article, long amount, BigDecimal overridePrice, OverrideReasonEnum overridePriceReason, BigDecimal overriddenDiscount, OverrideReasonEnum overrideDiscountReason) {
        this.receipt = receipt;
        this.article = article;
        this.amount = amount;
        this.overridePrice = overridePrice;
        this.overridePriceReason = overridePriceReason;
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

    public BigDecimal getOverridePrice() {
        return overridePrice;
    }

    public OverrideReasonEnum getOverridePriceReason() {
        return overridePriceReason;
    }

    public BigDecimal getOverriddenDiscount() {
        return overriddenDiscount;
    }

    public OverrideReasonEnum getOverrideDiscountReason() {
        return overrideDiscountReason;
    }
}
