package de.fhdw.vendix.store.core.domain.store_stock;

import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.domain.article.Article;
import de.fhdw.vendix.store.core.domain.store.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
public class StoreStock extends AbstractSpringDataAuditingEntity<Long> {

    @ManyToOne(optional = false)
    private Store store;

    @ManyToOne(optional = false)
    private Article article;

    @Min(value = 0)
    private long currentAmount;

    @NotNull
    @Min(value = 0)
    @Column(nullable = false)
    private long criticalAmount = 5; // Serves as an alert and definition for what quantifies as low stock

    private boolean isActive;

    protected StoreStock() {}

    protected StoreStock(Store store, Article article, long currentAmount, long criticalAmount, boolean isActive) {
        this.store = store;
        this.article = article;
        this.currentAmount = currentAmount;
        this.criticalAmount = criticalAmount;
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

    public long getCriticalAmount() {
        return criticalAmount;
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