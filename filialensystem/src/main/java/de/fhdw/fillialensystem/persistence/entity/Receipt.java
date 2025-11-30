package de.fhdw.fillialensystem.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;

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
    private List<ReceiptLinkArticle> receiptArticles = new ArrayList<>();

    public Receipt() {
        super();
    }

    public Receipt(Long id) {
        super(id);
    }

    public Receipt(Store store, Register register, Account account, BigDecimal totalAmount, List<ReceiptLinkArticle> receiptArticles) {
        this.store = store;
        this.register = register;
        this.account = account;
        this.totalAmount = totalAmount;
        this.receiptArticles = receiptArticles;
    }

    public Receipt(Long id, Store store, Register register, Account account, BigDecimal totalAmount, List<ReceiptLinkArticle> receiptArticles) {
        super(id);
        this.store = store;
        this.register = register;
        this.account = account;
        this.totalAmount = totalAmount;
        this.receiptArticles = receiptArticles;
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

    public List<ReceiptLinkArticle> getReceiptArticles() {
        return receiptArticles;
    }

    public void setReceiptArticles(List<ReceiptLinkArticle> receiptLinkArticle) {
        this.receiptArticles = receiptLinkArticle;
    }
}
