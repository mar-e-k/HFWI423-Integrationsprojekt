package com.example.application.data;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "stock_change_log")
public class StockChangeLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "article_number", nullable = false, length = 18)
    private String articleNumber;

    @Column(name = "article_name", nullable = false, length = 255)
    private String articleName;

    @Column(name = "old_stock", nullable = false)
    private Integer oldStock;

    @Column(name = "delta", nullable = false)
    private Integer delta;

    @Column(name = "new_stock", nullable = false)
    private Integer newStock;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private ChangeType changeType;

    @Column(name = "reason")
    private String reason;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt = OffsetDateTime.now();

    @Column(name = "changed_by")
    private String changedBy;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getArticleId() {
        return articleId;
    }
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
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

    public Integer getOldStock() {
        return oldStock;
    }
    public void setOldStock(Integer oldStock) {
        this.oldStock = oldStock;
    }

    public Integer getDelta() {
        return delta;
    }
    public void setDelta(Integer delta) {
        this.delta = delta;
    }

    public Integer getNewStock() {
        return newStock;
    }
    public void setNewStock(Integer newStock) {
        this.newStock = newStock;
    }

    public ChangeType getChangeType() {
        return changeType;
    }
    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }

    public OffsetDateTime getChangedAt() {
        return changedAt;
    }
    public void setChangedAt(OffsetDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }
    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }
}