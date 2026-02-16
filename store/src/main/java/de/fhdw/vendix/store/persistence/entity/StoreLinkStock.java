package de.fhdw.vendix.store.persistence.entity;

import de.fhdw.vendix.store.persistence.entity.imported.Article;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
public class StoreLinkStock extends AbstractEntity {

    @ManyToOne(optional = false)
    private Store store;

    @ManyToOne(optional = false)
    private Article article;

    @Min(value = 0, message = "Amount must be >= 0")
    private int amount;

    @NotNull(message = "Minimum stock level cannot be null")
    @Min(value = 0, message = "Minimum stock level must be >= 0")
    @Column(nullable = false)
    private int minimumStockLevel = 5; // Serves as an alert and defintion for what quantifies as low stock

    private boolean active;

    public StoreLinkStock() {
        super();
    }

    public StoreLinkStock(Store store, Article article, int amount, int minimumStockLevel, boolean active) {
        this.store = store;
        this.article = article;
        this.amount = amount;
        this.minimumStockLevel = minimumStockLevel;
        this.active = active;
    }

    public StoreLinkStock(Long id, Store store, Article article, int amount, int minimumStockLevel, boolean active) {
        super(id);
        this.store = store;
        this.article = article;
        this.amount = amount;
        this.minimumStockLevel = minimumStockLevel;
        this.active = active;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getMinimumStockLevel() {
        return minimumStockLevel;
    }

    public void setMinimumStockLevel(int minimumStockLevel) {
        this.minimumStockLevel = minimumStockLevel;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}