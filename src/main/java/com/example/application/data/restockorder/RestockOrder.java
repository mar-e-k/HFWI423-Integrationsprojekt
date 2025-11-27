package com.example.application.data.restockorder;

import com.example.application.data.AbstractEntity;
import com.example.application.data.article.ArticleInfo;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "restock_order")
public class RestockOrder extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "article_id")
    private ArticleInfo article;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "approved", nullable = false)
    private boolean approved;

    @Column(name = "delivered", nullable = false)
    private boolean delivered;

    // ===============================
    // Getter und Setter
    // ===============================

    public ArticleInfo getArticle() {
        return article;
    }

    public void setArticle(ArticleInfo article) {
        this.article = article;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }
}
