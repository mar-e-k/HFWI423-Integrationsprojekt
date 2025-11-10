package de.fhdw.kassensystem.view.cashier;

import de.fhdw.kassensystem.persistence.entity.imported.Article;

import java.math.BigDecimal;

public class CartItem {
    private Article article;
    private int position;
    private int quantity;
    private BigDecimal overriddenPrice;

    public CartItem(Article article, int position, int quantity, BigDecimal overriddenPrice) {
        this.article = article;
        this.position = position;
        this.quantity = quantity;
        this.overriddenPrice = overriddenPrice;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getOverriddenPrice() {
        return overriddenPrice;
    }

    public void setOverriddenPrice(BigDecimal overriddenPrice) {
        this.overriddenPrice = overriddenPrice;
    }

    public BigDecimal getEffectivePrice() {
        return overriddenPrice != null ? overriddenPrice : BigDecimal.valueOf(article.getSellingPrice());
    }

}