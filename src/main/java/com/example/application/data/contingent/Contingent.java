package com.example.application.data.contingent;

import jakarta.persistence.*;

@Entity
@Table(name = "contingent")
public class Contingent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referenz zum Artikel aus ArticleInfo
    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "supplier_id")
    private Long supplierId;

    // ------------------- Getter/Setter -------------------

    public Long getId() {
        return id;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }
}
