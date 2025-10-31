package com.example.application.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class ArticleInfo extends AbstractEntity {

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;
    @Size(max = 18)
    @NotNull
    @Column(name = "article_number", nullable = false, length = 18)
    private String articleNumber;

    @NotNull
    @Column(name = "stock_level", nullable = false)
    private Integer stockLevel;
    @Size(max = 255)
    @NotNull
    @Column(name = "storage_location", nullable = false)
    private String storageLocation;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getArticleNumber() {
        return articleNumber;
    }
    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }
    public Integer getStockLevel() { return stockLevel; }
    public void setStockLevel(Integer stockLevel) {
        this.stockLevel = stockLevel;
    }
    public String getStorageLocation() {
        return storageLocation;
    }
    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

}
