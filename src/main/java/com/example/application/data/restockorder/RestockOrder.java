package com.example.application.data.restockorder;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "restock_order", schema = "nachbestellung", indexes = {
    @Index(name = "idx_restock_number_delivered",  columnList = "article_number, delivered"),
    @Index(name = "idx_restock_delivered_approved", columnList = "delivered, approved")
})
public class RestockOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Fachliche Artikelnummer (z. B. "13")
    @Column(name = "article_number", nullable = false, length = 50)
    private String articleNumber;

    // Artikelbezeichnung (z. B. "Apfel")
    @Column(name = "article_name", nullable = false, length = 255)
    private String articleName;

    // Menge in Stück
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "approved", nullable = false)
    private boolean approved;

    @Column(name = "delivered", nullable = false)
    private boolean delivered;

    // ===============================
    // Getter / Setter
    // ===============================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public String getArticleName() {
        return articleName;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
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
