package com.example.application.data.goodsreceipts;

import com.example.application.data.AbstractEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
//hetuhghueghusghueguhieguhiegiuhe
@Entity
@Table(name = "goods_receipt", indexes = {
    @Index(name = "idx_goods_receipt_status", columnList = "status")
})
public class GoodsReceipt extends AbstractEntity {


    // Wird automatisch generiert (z. B. WE-2025-00001)
    @Column(name = "receipt_number", unique = true, nullable = false, length = 32)
    private String receiptNumber;

    @Column(name = "supplier_name", nullable = false, length = 200)
    private String supplierName;

    @Column(name = "delivery_note_number", nullable = false, length = 64)
    private String deliveryNoteNumber;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private GoodsReceiptStatus status = GoodsReceiptStatus.IN_PRUEFUNG;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public GoodsReceipt() {
        // JPA braucht einen No-Args-Konstruktor
    }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = GoodsReceiptStatus.IN_PRUEFUNG;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ---- Getter & Setter ----


    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getDeliveryNoteNumber() {
        return deliveryNoteNumber;
    }

    public void setDeliveryNoteNumber(String deliveryNoteNumber) {
        this.deliveryNoteNumber = deliveryNoteNumber;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public GoodsReceiptStatus getStatus() {
        return status;
    }

    public void setStatus(GoodsReceiptStatus status) {
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

    @Override
    public String toString() {
        return "GoodsReceipt{" +
                "receiptNumber='" + receiptNumber + '\'' +
                ", supplierName='" + supplierName + '\'' +
                ", deliveryNoteNumber='" + deliveryNoteNumber + '\'' +
                ", deliveryDate=" + deliveryDate +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
