package com.example.application.data.article;

public class RestockItem {
    private final ArticleInfo article;
    private final int orderAmount;

    public RestockItem(ArticleInfo article) {
        this.article = article;
        // Formel: (minStock * 2) - stockLevel
        this.orderAmount = (article.getMinStock() * 2) - article.getStockLevel();
    }

    public ArticleInfo getArticle() {
        return article;
    }

    public String getArticleNumber() {
        return article.getArticleNumber();
    }

    public String getName() {
        return article.getName();
    }

    public int getStockLevel() {
        return article.getStockLevel();
    }

    public int getMinStock() {
        return article.getMinStock();
    }

    public int getOrderAmount() {
        return orderAmount;
    }
}

