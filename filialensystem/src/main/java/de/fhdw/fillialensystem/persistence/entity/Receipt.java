package de.fhdw.fillialensystem.persistence.entity;

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

    @OneToMany(mappedBy = "receipt", orphanRemoval = true)
    private List<ReceiptLinkArticle> receiptLinkArticle = new ArrayList<>();

    @NotNull(message = "Total amount must not be null")
    private BigDecimal totalAmount;

    public Receipt() {
        super();
    }

    public Receipt(Store store, Register register, Account account, List<ReceiptLinkArticle> receiptLinkArticle, BigDecimal totalAmount) {
        this.store = store;
        this.register = register;
        this.account = account;
        this.receiptLinkArticle = receiptLinkArticle;
        this.totalAmount = totalAmount;
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

    public List<ReceiptLinkArticle> getReceiptLinkArticle() {
        return receiptLinkArticle;
    }

    public void setReceiptLinkArticle(List<ReceiptLinkArticle> receiptLinkArticle) {
        this.receiptLinkArticle = receiptLinkArticle;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
