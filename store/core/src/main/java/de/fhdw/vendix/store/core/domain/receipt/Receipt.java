package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;
import de.fhdw.vendix.store.core.domain.receipt_voucher.ReceiptVoucher;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;

@Entity
public class Receipt extends AbstractSpringDataAuditingEntity<Long> {

    private Long storeId;

    private Long registerId;

    @Column(nullable = false)
    private Long cashierId;

    @OneToMany(mappedBy = "receipt", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ReceiptLine> receiptLines = new ArrayList<>();

    @OneToMany(mappedBy = "receipt", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<ReceiptVoucher> receiptVouchers = new HashSet<>();

    protected Receipt() {}

    public Receipt(Long storeId, Long registerId, Long cashierId) {
        this.storeId = storeId;
        this.registerId = registerId;
        this.cashierId = cashierId;
    }

    @Default
    protected Receipt(@Nullable Long id, Long storeId, Long registerId, Long cashierId) {
        super(id);
        this.storeId = storeId;
        this.registerId = registerId;
        this.cashierId = cashierId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public Long getRegisterId() {
        return registerId;
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