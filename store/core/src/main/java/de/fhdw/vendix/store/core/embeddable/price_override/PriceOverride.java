package de.fhdw.vendix.store.core.embeddable.price_override;

import de.fhdw.vendix.commons.api.embeddable.OverrideReason;
import de.fhdw.vendix.commons.api.structure.mapper.Default;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Embeddable
@SuppressWarnings("NullAway")
public class PriceOverride {

    @NotNull(message = "Price override amount cannot be null")
    @DecimalMin(value = "0.00", message = "Price override amount must be at least 0.00")
    @Column(name = "price_override_amount")
    private BigDecimal amount;

    @NotNull(message = "Price override reason cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "price_override_reason")
    private OverrideReason reason;

    public PriceOverride() {}

    @Default
    public PriceOverride(BigDecimal amount, OverrideReason reason) {
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