package com.example.application.data.orderPicking;

import com.example.application.data.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class MessageLogistic {
    @Id
    private Integer id;
    //Artikelnummer
    @NotNull
    @Column(name = "article_number", nullable = false)
    private Integer articleNumber;

    // Lagerbestand des Artikels
    @Column(name = "store_id")
    private String storeId;

    @Column(name = "stock_level")
    private Integer stockLevel;

    @Column(name = "target_stock_level")
    private Integer targetStockLevel;

    public Integer getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(Integer articleNumber) {
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}