package com.example.application.data;

import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class ArticleInfo extends AbstractEntity {

    private String articleName;
    private Integer articleNumber;
    private Integer inventory;
    private String storageLocation;

    public String getArticleName() {
        return articleName;
    }
    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }
    public Integer getArticleNumber() {
        return articleNumber;
    }
    public void setArticleNumber(Integer articleNumber) {
        this.articleNumber = articleNumber;
    }
    public Integer getInventory() { return inventory; }
    public void setInventory(Integer inventory) {
        this.inventory = inventory;
    }
    public String getStorageLocation() {
        return storageLocation;
    }
    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

}
