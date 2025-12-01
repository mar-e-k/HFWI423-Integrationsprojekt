package de.fhdw.fillialensystem.persistence.entity;

import de.fhdw.fillialensystem.persistence.entity.imported.Article;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;

@Entity
public class StoreLinkStock extends AbstractEntity {

    @ManyToOne(optional = false)
    private Store store;

    @ManyToOne(optional = false)
    private Article article;

    @Min(value = 0, message = "Amount must be >= 0")
    private int amount;

    private boolean active;

    // Mindestbestand mit Default-Wert 5
    @NotNull
    @Column(
            name = "min_stock_level",
            nullable = false,
            columnDefinition = "integer default 5"
    )
    private Integer minStockLevel = 5;

    public StoreLinkStock() {
        super();
    }

    public StoreLinkStock(Store store, Article article, int amount, boolean active) {
        this.store = store;
        this.article = article;
        this.amount = amount;
        this.active = active;
        this.minStockLevel = 5;
    }

    public StoreLinkStock(Store store, Article article, int amount, boolean active, Integer minStockLevel) {
        this.store = store;
        this.article = article;
        this.amount = amount;
        this.active = active;
        this.minStockLevel = (minStockLevel != null ? minStockLevel : 5);
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(Integer minStockLevel) {
        this.minStockLevel = minStockLevel;
    }
}