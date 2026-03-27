package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "contingent")
public class Contingent {

    public Contingent(Long id, Long orderId, Long supplierId, Long articleId, Integer availableQuantity) {
        this.id = id;
        this.orderId = orderId;
        this.supplierId = supplierId;
        this.articleId = articleId;
        this.availableQuantity = availableQuantity;
    }

    public Contingent() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long supplierId;

    // Referenz zum Artikel
    @Column(nullable = false)
    private Long articleId;


    @Column(nullable = false)
    private Integer availableQuantity;
}
