package de.fhdw.vendix.store.core.domain.receipt_voucher;

import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.domain.receipt.Receipt;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class ReceiptVoucher extends AbstractSpringDataAuditingEntity<Long> {

    @NotNull
    @OneToMany(mappedBy = "receiptVoucher")
    private List<Receipt> receipt = new ArrayList<>();

    @Column(nullable = false, unique = true)
    @NotNull
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID code;

    @Column(nullable = false)
    private boolean isRedeemed;

    @Column(nullable = false)
    @NotNull
    private Instant redeemedAt;

    @Column(nullable = false)
    @NotNull
    private Instant expiresAt;

    protected ReceiptVoucher() {}

    public ReceiptVoucher(List<Receipt> receipt, UUID code, boolean isRedeemed, Instant redeemedAt, Instant expiresAt) {
        this.receipt = receipt;
        this.code = code;
        this.isRedeemed = isRedeemed;
        this.redeemedAt = redeemedAt;
        this.expiresAt = expiresAt;
    }

    public List<Receipt> getReceipt() {
        return receipt;
    }

    public UUID getCode() {
        return code;
    }

    public boolean isRedeemed() {
        return isRedeemed;
    }

    public Instant getRedeemedAt() {
        return redeemedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}