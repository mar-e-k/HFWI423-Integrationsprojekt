package de.fhdw.vendix.store.core.domain.store_stock;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.persistance.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.embeddable.preference_amount.PreferenceAmount;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_store_stock_store_article", columnNames = {"store_id", "article_id"})
        },
        indexes = {
                @Index(name = "idx_store_stock_store_article", columnList = "store_id, article_id")
        }
)
public class StoreStock extends AbstractSpringDataAuditingEntity<Long> {

    @NotNull(message = "Store ID cannot be null")
    @Min(value = 1, message = "Store ID must be at least 1")
    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @NotNull(message = "Article ID cannot be null")
    @Min(value = 1, message = "Article ID must be at least 1")
    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @NotNull(message = "Current amount cannot be null")
    @Min(value = 0, message = "Current amount must be at least 1")
    @Column(name = "current_amount", nullable = false)
    private Long currentAmount;

    @NotNull(message = "Preference amount cannot be null")
    @Embedded
    private PreferenceAmount preferenceAmount;

    protected StoreStock() {}

    protected StoreStock(Long storeId, Long articleId, Long currentAmount, PreferenceAmount preferenceAmount) {
        this.storeId = storeId;
        this.articleId = articleId;
        this.currentAmount = currentAmount;
        this.preferenceAmount = preferenceAmount;
    }

    @Default
    protected StoreStock(
            @Nullable Long id,
            Long storeId,
            Long articleId,
            Long currentAmount,
            PreferenceAmount preferenceAmount
    ) {
        super(id);
        this.storeId = storeId;
        this.articleId = articleId;
        this.currentAmount = currentAmount;
        this.preferenceAmount = preferenceAmount;
    }

    public Long getStoreId() {
        return storeId;
    }

    public Long getArticleId() {
        return articleId;
    }

    public Long getCurrentAmount() {
        return currentAmount;
    }

    public PreferenceAmount getPreferenceAmount() {
        return preferenceAmount;
    }

    public void restockArticle(Long amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("Parameter 'articleAmount' must be greater than 0.");
        }
        this.currentAmount += amount;
    }
}
