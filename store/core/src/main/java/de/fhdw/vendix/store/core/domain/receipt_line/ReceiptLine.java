package de.fhdw.vendix.store.core.domain.receipt_line;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.domain.article.Article;
import de.fhdw.vendix.store.core.domain.receipt.Receipt;
import de.fhdw.vendix.store.core.embeddable.discount_override.DiscountOverride;
import de.fhdw.vendix.store.core.embeddable.price_override.PriceOverride;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.jspecify.annotations.Nullable;

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
    private Long amount;

    @Embedded
    @Nullable
    private DiscountOverride discountOverride;

    @Embedded
    @Nullable
    private PriceOverride priceOverride;

    protected ReceiptLine() {}

    protected ReceiptLine(Receipt receipt, Article article, Long amount) {
        this.receipt = receipt;
        this.article = article;
        this.amount = amount;
    }

    protected ReceiptLine(@Nullable Long id, Receipt receipt, Article article, Long amount) {
        super(id);
        this.receipt = receipt;
        this.article = article;
        this.amount = amount;
    }

    protected ReceiptLine(Receipt receipt, Article article, Long amount, @Nullable DiscountOverride discountOverride, @Nullable PriceOverride priceOverride) {
        this.receipt = receipt;
        this.article = article;
        this.amount = amount;
        this.discountOverride = discountOverride;
        this.priceOverride = priceOverride;
    }

    @Default
    protected ReceiptLine(@Nullable Long id, Receipt receipt, Article article, Long amount, @Nullable DiscountOverride discountOverride, @Nullable PriceOverride priceOverride) {
        super(id);
        this.receipt = receipt;
        this.article = article;
        this.amount = amount;
        this.discountOverride = discountOverride;
        this.priceOverride = priceOverride;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public Article getArticle() {
        return article;
    }

    public Long getAmount() {
        return amount;
    }

    public @Nullable DiscountOverride getDiscountOverride() {
        return discountOverride;
    }

    public @Nullable PriceOverride getPriceOverride() {
        return priceOverride;
    }
}