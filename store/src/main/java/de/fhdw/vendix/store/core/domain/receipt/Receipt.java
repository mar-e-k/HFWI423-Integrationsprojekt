package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptRequestDTO;
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

    @OneToMany(mappedBy = "receipt", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ReceiptLine> receiptLines = new ArrayList<>();

    @OneToMany(mappedBy = "receipt", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ReceiptVoucher> receiptVouchers = new ArrayList<>();

    protected Receipt() {}

    public Receipt(ReceiptRequestDTO dto) {

    }

    public Receipt(Store store, Register register, Account account, List<ReceiptLine> receiptLines, List<ReceiptVoucher> receiptVouchers) {
        this.store = store;
        this.register = register;
        this.account = account;
        this.receiptLines = receiptLines;
        this.receiptVouchers = receiptVouchers;
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

    public List<ReceiptLine> getReceiptLines() {
        return receiptLines;
    }

    public List<ReceiptVoucher> getReceiptVouchers() {
        return receiptVouchers;
    }
}