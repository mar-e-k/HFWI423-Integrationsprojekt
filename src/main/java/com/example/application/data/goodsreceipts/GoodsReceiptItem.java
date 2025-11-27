package com.example.application.data.goodsreceipts;


import com.example.application.data.article.ArticleInfo;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "goods_receipt_item")
public class GoodsReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Bezug zum Wareneingang
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    private GoodsReceipt goodsReceipt;

    // Bezug zum Artikel (ArticleInfo)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_info_id", nullable = false)
    private ArticleInfo article;

    // Erwartete Menge laut Lieferschein / Bestellung
    @Column(name = "expected_qty")
    private Integer expectedQuantity;

    // Tatsächlich gelieferte Menge (Pflicht)
    @Column(name = "actual_qty", nullable = false)
    private Integer actualQuantity;

    // Mängelbeschreibung (optional)
    @Column(name = "defect_notes", length = 1000)
    private String defectNotes;

    // Prüfstatus des einzelnen Artikels
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private GoodsReceiptItemStatus status = GoodsReceiptItemStatus.IN_PRUEFUNG;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public GoodsReceiptItem() {
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getter & Setter ---

    public Long getId() {
        return id;
    }

    public GoodsReceipt getGoodsReceipt() {
        return goodsReceipt;
    }

    public void setGoodsReceipt(GoodsReceipt goodsReceipt) {
        this.goodsReceipt = goodsReceipt;
    }

    public ArticleInfo getArticle() {
        return article;
    }

    public void setArticle(ArticleInfo article) {
        this.article = article;
    }

    public Integer getExpectedQuantity() {
        return expectedQuantity;
    }

    public void setExpectedQuantity(Integer expectedQuantity) {
        this.expectedQuantity = expectedQuantity;
    }

    public Integer getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(Integer actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public String getDefectNotes() {
        return defectNotes;
    }

    public void setDefectNotes(String defectNotes) {
        this.defectNotes = defectNotes;
    }

    public GoodsReceiptItemStatus getStatus() {
        return status;
    }

    public void setStatus(GoodsReceiptItemStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}