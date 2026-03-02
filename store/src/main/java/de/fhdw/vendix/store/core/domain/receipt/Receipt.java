package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.domain.account.Account;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptArticle;
import de.fhdw.vendix.store.core.domain.register.Register;
import de.fhdw.vendix.store.core.domain.store.Store;
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
public class Receipt extends AbstractSpringDataAuditingEntity<Long> {

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

    @Column(name = "deposit_redemption_code", length = 5)
    private String depositRedemptionCode;

    public Receipt() {
        super();
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

    public Store getStore() {
        return store;
    }

    public Register getRegister() {
        return register;
    }

    public Account getAccount() {
        return account;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<ReceiptArticle> getReceiptArticles() {
        return receiptArticles;
    }

    public boolean isDepositOnly() {
        return isDepositOnly;
    }

    public String getDepositRedemptionCode() {
        return depositRedemptionCode;
    }
}
