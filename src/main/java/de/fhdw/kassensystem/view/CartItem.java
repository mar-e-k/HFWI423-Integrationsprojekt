package de.fhdw.kassensystem.view;

import de.fhdw.kassensystem.persistence.entity.imported.Article;

import java.io.Serializable;

public class CartItem implements Serializable {
    private int position;
    private final Article article;
    private int quantity;
    private Double overriddenPrice;

    public CartItem(int position, Article article, int quantity) {
        this.position = position;
        this.article = article;
        this.quantity = quantity;
    }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public Article getArticle() { return article; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Double getOverriddenPrice() { return overriddenPrice; }
    public void setOverriddenPrice(Double overriddenPrice) { this.overriddenPrice = overriddenPrice; }
    public double getEffectivePrice() {
        return overriddenPrice != null ? overriddenPrice : article.getSellingPrice();
    }
}
