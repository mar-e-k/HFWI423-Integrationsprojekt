package com.example.application.data.contingent;

import com.example.application.data.AbstractEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "contingent")
public class Contingent extends AbstractEntity {

    @Column (name = "article_id", nullable = false)
    private Long article;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "supplier_id")
    private Long supplierId;

    // Getter/Setter
    public Long getArticle() {
        return article;
    }

    public void setArticle(Long article) {
        this.article = article;
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
