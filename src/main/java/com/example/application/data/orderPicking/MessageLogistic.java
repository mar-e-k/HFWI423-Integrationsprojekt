package com.example.application.data.orderPicking;

import com.example.application.data.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "message_logistic", schema = "kommission", indexes = {
    @Index(name = "idx_msglogistic_kommission_id",   columnList = "kommission_id"),
    @Index(name = "idx_msglogistic_store_processed", columnList = "store_id, processed")
})
public class MessageLogistic extends AbstractEntity {
    //Artikelnummer
    @NotNull
    @Column(name = "article_number", nullable = false)
    private String articleNumber;

    @Column(name = "article_id")
    private Long articleId;

    // Lagerbestand des Artikels
    @Column(name = "store_id")
    private String storeId;

    @Column(name = "quantity")
    private long quantity;

    @Column(name = "processed")
    private boolean processed;

    @ManyToOne
    @JoinColumn(name = "kommission_id")
    private Kommission kommission;

    @Column(name = "comment")
    private String comment;

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

    public Kommission getKommission() {
        return kommission;
    }

    public void setKommission(Kommission kommission) {this.kommission = kommission;}

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String getComment() {return  comment;}

    public void setComment(String comment) {this.comment = comment;}
}