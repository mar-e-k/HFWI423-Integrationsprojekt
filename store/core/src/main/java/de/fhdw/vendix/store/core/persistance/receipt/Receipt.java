package de.fhdw.vendix.store.core.persistance.receipt;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.persistance.account.Account;
import de.fhdw.vendix.store.core.persistance.receipt_line.ReceiptLine;
import de.fhdw.vendix.store.core.persistance.receipt_voucher.ReceiptVoucher;
import de.fhdw.vendix.store.core.persistance.register.Register;
import de.fhdw.vendix.store.core.persistance.store.Store;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;

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
    private Account cashier;

    @OneToMany(mappedBy = "receipt", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ReceiptLine> receiptLines = new ArrayList<>();

    @OneToMany(mappedBy = "receipt", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<ReceiptVoucher> receiptVouchers = new HashSet<>();

    protected Receipt() {}

    protected Receipt(Store store, Register register, Account cashier) {
        this.store = store;
        this.register = register;
        this.cashier = cashier;
    }

    @Default
    protected Receipt(@Nullable Long id, Store store, Register register, Account cashier) {
        super(id);
        this.store = store;
        this.register = register;
        this.cashier = cashier;
    }

    public Store getStore() {
        return store;
    }

    public Register getRegister() {
        return register;
    }

    public Account getCashier() {
        return cashier;
    }

    public List<ReceiptLine> getReceiptLines() {
        return Collections.unmodifiableList(receiptLines);
    }

    public Set<ReceiptVoucher> getReceiptVouchers() {
        return Collections.unmodifiableSet(receiptVouchers);
    }

    // TODO
    // Should include discounts and taxing
    public BigDecimal getTotalPrice() {
        return BigDecimal.ZERO;
    }
}