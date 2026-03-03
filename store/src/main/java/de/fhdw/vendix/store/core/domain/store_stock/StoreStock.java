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
    private int amount;

    @NotNull
    @Min(value = 0)
    @Column(nullable = false)
    private int minimumStockLevel = 5; // Serves as an alert and definition for what quantifies as low stock

    private boolean isActive;

    protected StoreStock() {}

    public StoreStock(Store store, Article article, int amount, int minimumStockLevel, boolean isActive) {
        this.store = store;
        this.article = article;
        this.amount = amount;
        this.minimumStockLevel = minimumStockLevel;
        this.isActive = isActive;
    }

    public Store getStore() {
        return store;
    }

    public Article getArticle() {
        return article;
    }

    public int getAmount() {
        return amount;
    }

    public int getMinimumStockLevel() {
        return minimumStockLevel;
    }

    public boolean isActive() {
        return isActive;
    }
}