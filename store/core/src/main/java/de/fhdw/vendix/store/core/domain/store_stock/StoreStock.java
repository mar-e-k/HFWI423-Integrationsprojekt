package de.fhdw.vendix.store.core.domain.store_stock;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.embeddable.preference_amount.PreferenceAmount;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@Entity

public class StoreStock extends AbstractSpringDataAuditingEntity<Long> {

    @NotNull(message = "Store ID cannot be null")
    @Min(value = 1, message = "Store ID must be at least 1")
    @Column(nullable = false)
    private Long storeId;

    @NotNull(message = "Article ID cannot be null")
    @Min(value = 1, message = "Article ID must be at least 1")
    @Column(nullable = false)
    private Long articleId;

    @NotNull(message = "Current amount cannot be null")
    @Min(value = 0, message = "Current amount must be at least 1")
    @Column(nullable = false)
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