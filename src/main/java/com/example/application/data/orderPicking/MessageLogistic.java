package com.example.application.data.orderPicking;

import com.example.application.data.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class MessageLogistic extends AbstractEntity {
    //Artikelnummer
    @NotNull
    @Column(name = "article_number", nullable = false)
    private String articleNumber;

    // Lagerbestand des Artikels
    @Column(name = "store_id")
    private String storeId;

    @Column(name = "stock_level")
    private Integer stockLevel;

    @Column(name = "target_stock_level")
    private Integer targetStockLevel;

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public Integer getStockLevel() {
        return stockLevel;
    }

    public void setStockLevel(Integer stockLevel) {
        this.stockLevel = stockLevel;
    }

    public Integer getTargetStockLevel() {
        return targetStockLevel;
    }

    public void setTargetStockLevel(Integer targetStockLevel) {
        this.targetStockLevel = targetStockLevel;
    }

}