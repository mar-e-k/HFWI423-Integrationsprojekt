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

    @Column(name = "quantity")
    private long quantity;

    @Column(name = "processed")
    private boolean processed;

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

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

}