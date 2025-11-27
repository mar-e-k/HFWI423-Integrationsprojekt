package com.example.application.data.contingent;

import com.example.application.data.AbstractEntity;
import com.example.application.data.article.ArticleInfo;
import jakarta.persistence.*;

@Entity
@Table(name = "contingent")
public class Contingent extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleInfo article;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "supplier_id")
    private Long supplierId;

    // Getter/Setter
    public ArticleInfo getArticle() {
        return article;
    }

    public void setArticle(ArticleInfo article) {
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
