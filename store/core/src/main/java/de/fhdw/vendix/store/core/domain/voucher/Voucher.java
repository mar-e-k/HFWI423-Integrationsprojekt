package de.fhdw.vendix.store.core.domain.voucher;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@Entity
public class Voucher extends AbstractSpringDataAuditingEntity<Long> {

    @NotNull(message = "Code cannot be null")
    @Column(nullable = false, unique = true)
    private UUID code;

    @Future(message = "Expires at must be in the future")
    @Nullable
    private Instant expiresAt;

    @Nullable
    private Instant redeemedAt;

    protected Voucher() {}

    protected Voucher(UUID code) {
        this.code = code;
    }

    public Voucher(@Nullable Long id, UUID code) {
        super(id);
        this.code = code;
    }

    public Voucher(UUID code, @Nullable Instant expiresAt, @Nullable Instant redeemedAt) {
        this.code = code;
        this.expiresAt = expiresAt;
        this.redeemedAt = redeemedAt;
    }

    @Default
    protected Voucher(@Nullable Long id, UUID code, @Nullable Instant expiresAt, @Nullable Instant redeemedAt) {
        super(id);
        this.code = code;
        this.expiresAt = expiresAt;
        this.redeemedAt = redeemedAt;
    }

    public UUID getCode() {
        return code;
    }

    public @Nullable Instant getExpiresAt() {
        return expiresAt;
    }

    public @Nullable Instant getRedeemedAt() {
        return redeemedAt;
    }

    public Voucher redeem() throws VoucherExpiredException, VoucherAlreadyRedeemedException {
        if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
            throw new VoucherExpiredException("Voucher has already expired and cannot be redeemed");
        }
        if (redeemedAt != null) {
            throw new VoucherAlreadyRedeemedException("Voucher already redeemed");
        }
        redeemedAt = Instant.now();
        return this;
    }
}