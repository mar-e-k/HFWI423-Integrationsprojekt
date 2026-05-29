package com.example.application.data.contingent;

import jakarta.persistence.*;

/**
 * Temporäre Lasttest-Tabelle – strukturell identisch mit {@link Contingent},
 * aber vollständig getrennt von Produktionsdaten.
 * Nach dem Test einfach per DELETE /api/load/contingents/simulate oder
 * direkt in Neon löschen: DROP TABLE contingent_lasttest;
 */
@Entity
@Table(name = "contingent_lasttest", schema = "kontingent", indexes = {
    @Index(name = "idx_contingent_lt_article_id",  columnList = "article_id"),
    @Index(name = "idx_contingent_lt_sim_number",  columnList = "sim_article_number")
})
public class ContingentLasttest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "supplier_id")
    private Long supplierId;

    /** Nur bei synthetisch simulierten neuen Artikeln gesetzt (kein ExternalArticle vorhanden) */
    @Column(name = "sim_article_number")
    private String simArticleNumber;

    @Column(name = "sim_article_name")
    private String simArticleName;

    // ------------------- Getter/Setter -------------------

    public Long getId() { return id; }

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public String getSimArticleNumber() { return simArticleNumber; }
    public void setSimArticleNumber(String simArticleNumber) { this.simArticleNumber = simArticleNumber; }

    public String getSimArticleName() { return simArticleName; }
    public void setSimArticleName(String simArticleName) { this.simArticleName = simArticleName; }

    public boolean isSynthetic() { return simArticleNumber != null; }
}
