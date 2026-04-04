package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;
import de.fhdw.vendix.store.core.domain.receipt_voucher.ReceiptVoucher;
import de.fhdw.vendix.store.core.domain.register.Register;
import de.fhdw.vendix.store.core.domain.store.Store;
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

    @Column(nullable = false)
    private Long cashierId;

    @OneToMany(mappedBy = "receipt", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ReceiptLine> receiptLines = new ArrayList<>();

    @OneToMany(mappedBy = "receipt", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<ReceiptVoucher> receiptVouchers = new HashSet<>();

    protected Receipt() {}

    protected Receipt(Store store, Register register, Long cashierId) {
        this.store = store;
        this.register = register;
        this.cashierId = cashierId;
    }

    @Default
    protected Receipt(@Nullable Long id, Store store, Register register, Long cashierId) {
        super(id);
        this.store = store;
        this.register = register;
        this.cashierId = cashierId;
    }

    public Store getStore() {
        return store;
    }

    public Register getRegister() {
        return register;
    }

    public Long getCashierId() {
        return cashierId;
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