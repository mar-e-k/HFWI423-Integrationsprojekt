package de.fhdw.vendix.store.core.persistance.store_stock;

import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.persistance.article.Article;
import de.fhdw.vendix.store.core.persistance.store.Store;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"store", "article"}))
public class StoreStock extends AbstractSpringDataAuditingEntity<Long> {

    @ManyToOne(optional = false)
    private Store store;

    @ManyToOne(optional = false)
    private Article article;

    @Min(value = 0)
    private long currentAmount;

    @Embedded
    @NotNull
    private PreferenceAmount preferenceAmount;

    private boolean isActive;

    protected StoreStock() {}

    protected StoreStock(Store store, Article article, long currentAmount, PreferenceAmount preferenceAmount, boolean isActive) {
        this.store = store;
        this.article = article;
        this.currentAmount = currentAmount;
        this.preferenceAmount = preferenceAmount;
        this.isActive = isActive;
    }

    public Store getStore() {
        return store;
    }

    public Article getArticle() {
        return article;
    }

    public long getCurrentAmount() {
        return currentAmount;
    }

    public PreferenceAmount getPreferenceAmount() {
        return preferenceAmount;
    }

    public boolean isActive() {
        return isActive;
    }

    protected void restockArticle(long amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("Parameter 'amount' must be greater than 0.");
        }
        this.currentAmount += amount;
    }
}