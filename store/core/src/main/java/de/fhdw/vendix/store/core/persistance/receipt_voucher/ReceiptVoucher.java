package de.fhdw.vendix.store.core.persistance.receipt_voucher;

import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.persistance.receipt.Receipt;
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
    private UUID code;

    private Instant expiresAt;

    private Instant redeemedAt;

    protected ReceiptVoucher() {}

    protected ReceiptVoucher(Receipt receipt, UUID code, Instant expiresAt, Instant redeemedAt) {
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

    public ReceiptVoucher redeem() {
        if (expiresAt.isBefore(Instant.now())) {
            throw new VoucherExpiredException("Voucher has already expired at '%s'".formatted(expiresAt.toString()));
        }
        if (redeemedAt != null) {
            throw new VoucherAlreadyRedeemedException("Voucher already redeemed");
        }
        redeemedAt = Instant.now();
        return this;
    }
}