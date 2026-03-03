package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.domain.account.Account;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;
import de.fhdw.vendix.store.core.domain.receipt_voucher.ReceiptVoucher;
import de.fhdw.vendix.store.core.domain.register.Register;
import de.fhdw.vendix.store.core.domain.store.Store;
import jakarta.persistence.*;

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

    @ManyToOne(optional = true)
    @JoinColumn(nullable = true)
    private ReceiptVoucher receiptVoucher;

    @OneToMany(mappedBy = "receipt", orphanRemoval = true)
    private List<ReceiptLine> receiptLines = new ArrayList<>();


    public Receipt() {
        super();
    }

    public Receipt(Store store, Register register, Account account, ReceiptVoucher receiptVoucher, List<ReceiptLine> receiptLines) {
        this.store = store;
        this.register = register;
        this.account = account;
        this.receiptVoucher = receiptVoucher;
        this.receiptLines = receiptLines;
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

    public ReceiptVoucher getReceiptVoucher() {
        return receiptVoucher;
    }

    public List<ReceiptLine> getReceiptLines() {
        return receiptLines;
    }
}