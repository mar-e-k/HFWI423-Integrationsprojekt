package de.fhdw.fillialensystem.persistence.entity;

import de.fhdw.commons.api.dto.DepositStatus;
import de.fhdw.commons.persistence.entity.GenericEntity;
import de.fhdw.fillialensystem.persistence.entity.imported.Article;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
public class ReceiptArticle implements GenericEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Receipt receipt;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Article article;

    // ---- Required ----
    @NotNull(message = "Price cannot be null")
    private BigDecimal price;

    @NotNull(message = "Amount cannot be null")
    private Integer amount; // Amount can be negative, for instance, when returning items

    @DecimalMin(value = "0.00", message = "Tax rate must >= 0.00")
    @DecimalMax(value = "100.00", message = "Tax rate must <= 100.00")
    @NotNull(message = "Tax rate cannot be null")
    private BigDecimal taxRate;

    // ---- Optional ----
    private BigDecimal overridePrice;

    @Size(max = 255)
    private String overrideReason; // Might want to change to enum

    @DecimalMin(value = "0.00", message = "Discounted amount must be >= 0.00")
    @DecimalMax(value = "100.00", message = "Discounted amount must be <= 100.00")
    private BigDecimal discountedByPercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "deposit_status", nullable = false)
    @NotNull(message = "Deposit status cannot be null")
    private DepositStatus depositStatus = DepositStatus.NONE;

    public ReceiptArticle() {
        super();
    }

    public ReceiptArticle(Receipt receipt, Article article, BigDecimal price, Integer amount, BigDecimal taxRate, BigDecimal overridePrice, String overrideReason, BigDecimal discountedByPercent, DepositStatus depositStatus) {
        this.receipt = receipt;
        this.article = article;
        this.price = price;
        this.amount = amount;
        this.taxRate = taxRate;
        this.overridePrice = overridePrice;
        this.overrideReason = overrideReason;
        this.discountedByPercent = discountedByPercent;
        this.depositStatus = depositStatus;
    }

    public ReceiptArticle(Long id, Receipt receipt, Article article, BigDecimal price, Integer amount, BigDecimal taxRate, BigDecimal overridePrice, String overrideReason, BigDecimal discountedByPercent, DepositStatus depositStatus) {
        this.id = id;
        this.receipt = receipt;
        this.article = article;
        this.price = price;
        this.amount = amount;
        this.taxRate = taxRate;
        this.overridePrice = overridePrice;
        this.overrideReason = overrideReason;
        this.discountedByPercent = discountedByPercent;
        this.depositStatus = depositStatus;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getOverridePrice() {
        return overridePrice;
    }

    public void setOverridePrice(BigDecimal overridePrice) {
        this.overridePrice = overridePrice;
    }

    public String getOverrideReason() {
        return overrideReason;
    }

    public void setOverrideReason(String overrideReason) {
        this.overrideReason = overrideReason;
    }

    public BigDecimal getDiscountedByPercent() {
        return discountedByPercent;
    }

    public void setDiscountedByPercent(BigDecimal discountedByPercent) {
        this.discountedByPercent = discountedByPercent;
    }

    public DepositStatus getDepositStatus() {
        return depositStatus;
    }

    public void setDepositStatus(DepositStatus depositStatus) {
        this.depositStatus = depositStatus;
    }
}
