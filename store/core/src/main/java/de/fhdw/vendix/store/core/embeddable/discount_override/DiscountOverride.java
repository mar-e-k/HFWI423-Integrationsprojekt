package de.fhdw.vendix.store.core.embeddable.discount_override;

import de.fhdw.vendix.commons.api.embeddable.OverrideReason;
import de.fhdw.vendix.commons.api.structure.mapper.Default;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Embeddable
@SuppressWarnings("NullAway")
public class DiscountOverride {

    @NotNull(message = "Discount override amount cannot be null")
    @DecimalMin(value = "0.00", message = "Discount override amount must be at least 0.00")
    @DecimalMax(value = "100.00", message = "Discount override amount must be at most 100.00")
    @Column(name = "discount_override_amount")
    private BigDecimal amount;

    @NotNull(message = "Discount override reason cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_override_reason")
    private OverrideReason reason;

    public DiscountOverride() {}

    @Default
    public DiscountOverride(BigDecimal amount, OverrideReason reason) {
        this.amount = amount;
        this.reason = reason;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public OverrideReason getReason() {
        return reason;
    }
}