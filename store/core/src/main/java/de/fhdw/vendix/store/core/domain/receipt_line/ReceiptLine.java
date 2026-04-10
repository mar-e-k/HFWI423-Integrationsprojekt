package de.fhdw.vendix.store.core.domain.receipt_line;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.embeddable.discount_override.DiscountOverride;
import de.fhdw.vendix.store.core.embeddable.price_override.PriceOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@Entity
public class ReceiptLine extends AbstractSpringDataAuditingEntity<Long> {

    @NotNull(message = "Receipt ID cannot be null")
    @Min(value = 1, message = "Receipt ID must be at least 1")
    @Column(nullable = false)
    private Long receiptId;

    @NotNull(message = "Article ID cannot be null")
    @Min(value = 1, message = "Article ID must be at least 1")
    @Column(nullable = false)
    private Long articleId;

    @NotNull(message = "Article amount cannot be null")
    @Min(value = 1, message = "Article amount must be at least 1")
    @Column(nullable = false)
    private Long articleAmount;

    @Embedded
    @Nullable
    private DiscountOverride discountOverride;

    @Embedded
    @Nullable
    private PriceOverride priceOverride;

    protected ReceiptLine() {}

    protected ReceiptLine(Long receiptId, Long articleId, Long articleAmount) {
        this.receiptId = receiptId;
        this.articleId = articleId;
        this.articleAmount = articleAmount;
    }

    protected ReceiptLine(@Nullable Long id, Long receiptId, Long articleId, Long articleAmount) {
        super(id);
        this.receiptId = receiptId;
        this.articleId = articleId;
        this.articleAmount = articleAmount;
    }

    protected ReceiptLine(Long receiptId, Long articleId, Long articleAmount, @Nullable DiscountOverride discountOverride, @Nullable PriceOverride priceOverride) {
        this.receiptId = receiptId;
        this.articleId = articleId;
        this.articleAmount = articleAmount;
        this.discountOverride = discountOverride;
        this.priceOverride = priceOverride;
    }

    @Default
    protected ReceiptLine(@Nullable Long id, Long receiptId, Long articleId, Long articleAmount, @Nullable DiscountOverride discountOverride, @Nullable PriceOverride priceOverride) {
        super(id);
        this.receiptId = receiptId;
        this.articleId = articleId;
        this.articleAmount = articleAmount;
        this.discountOverride = discountOverride;
        this.priceOverride = priceOverride;
    }

    public Long getReceiptId() {
        return receiptId;
    }

    public Long getArticleId() {
        return articleId;
    }

    public Long getArticleAmount() {
        return articleAmount;
    }

    public @Nullable DiscountOverride getDiscountOverride() {
        return discountOverride;
    }

    public @Nullable PriceOverride getPriceOverride() {
        return priceOverride;
    }
}