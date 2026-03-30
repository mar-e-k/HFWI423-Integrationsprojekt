package de.fhdw.vendix.store.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Receipt extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Store store;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Register register;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Account account;

    @NotNull
    @Column(nullable = false)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "receipt", orphanRemoval = true)
    private List<ReceiptArticle> receiptArticles = new ArrayList<>();

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_deposit_only", nullable = false)
    private boolean isDepositOnly = false;

    @Column(unique = true)
    private String depositRedemptionCode;

    public Receipt() {
        super();
    }

    public Receipt(Long id) {
        super(id);
    }

    public Receipt(Store store, Register register, Account account, BigDecimal totalAmount, List<ReceiptArticle> receiptArticles, boolean isDepositOnly, String depositRedemptionCode) {
        this.store = store;
        this.register = register;
        this.account = account;
        this.totalAmount = totalAmount;
        this.receiptArticles = receiptArticles;
        this.isDepositOnly = isDepositOnly;
        this.depositRedemptionCode = depositRedemptionCode;
    }

    public Receipt(Long id, Store store, Register register, Account account, BigDecimal totalAmount, List<ReceiptArticle> receiptArticles, boolean isDepositOnly, String depositRedemptionCode) {
        super(id);
        this.store = store;
        this.register = register;
        this.account = account;
        this.totalAmount = totalAmount;
        this.receiptArticles = receiptArticles;
        this.isDepositOnly = isDepositOnly;
        this.depositRedemptionCode = depositRedemptionCode;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Register getRegister() {
        return register;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<ReceiptArticle> getReceiptArticles() {
        return receiptArticles;
    }

    public void setReceiptArticles(List<ReceiptArticle> receiptArticle) {
        this.receiptArticles = receiptArticle;
    }

    public boolean isDepositOnly() {
        return isDepositOnly;
    }

    public void setDepositOnly(boolean depositOnly) {
        isDepositOnly = depositOnly;
    }

    public String getDepositRedemptionCode() {
        return depositRedemptionCode;
    }

    public void setDepositRedemptionCode(String depositRedemptionCode) {
        this.depositRedemptionCode = depositRedemptionCode;
    }
}
