package com.example.application.data.articleInfo;

public class RestockItem {
    private final ArticleInfo article;
    private final Integer orderAmount;

    public RestockItem(ArticleInfo article) {
        this.article = article;
        Integer min = article.getMinStock(); // darf null sein
        if (min == null) {
            // Kein Mindestbestand gesetzt → keine Nachbestellmenge berechenbar
            this.orderAmount = null;
        } else {
            // Formel: (minStock * 2) - totalStock (inkl. Reservepaletten)
            this.orderAmount = (min * 2) - article.getTotalStock();
        }
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

    public Integer getReservePallets() {return article.getReservePallets();}

    public Integer getMinStock() {
        return article.getMinStock();
    }

    public Integer getOrderAmount() {
        return orderAmount;
    }

    public String getMinStockDisplay() {
        Integer min = article.getMinStock();
        return (min == null) ? "-" : String.valueOf(min);
    }

    public String getOrderAmountDisplay() {
        return (orderAmount == null) ? "-" : String.valueOf(orderAmount);
    }
}

