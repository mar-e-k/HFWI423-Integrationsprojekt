package de.fhdw.vendix.store.core.domain.receipt_voucher;

import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.domain.receipt.Receipt;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Entity
public class ReceiptVoucher extends AbstractSpringDataAuditingEntity<Long> {

    @NotNull
    @ManyToOne(optional = false)
    private Receipt receipt;

    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID code;

    private Instant expiresAt;

    private Instant redeemedAt;

    protected ReceiptVoucher() {}

    public ReceiptVoucher(Receipt receipt, UUID code, Instant expiresAt, Instant redeemedAt) {
        this.receipt = receipt;
        this.code = code;
        this.expiresAt = expiresAt;
        this.redeemedAt = redeemedAt;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public UUID getCode() {
        return code;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRedeemedAt() {
        return redeemedAt;
    }
}