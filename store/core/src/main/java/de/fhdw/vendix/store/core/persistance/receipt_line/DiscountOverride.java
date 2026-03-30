package de.fhdw.vendix.store.core.persistance.receipt_line;

import de.fhdw.vendix.commons.api.domain.receipt_line.OverrideReason;
import de.fhdw.vendix.commons.api.structure.mapper.Default;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

@Embeddable
@SuppressWarnings("NullAway")
public class DiscountOverride {

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    @Column(name = "discount_override_amount")
    private BigDecimal discount;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_override_reason")
    private OverrideReason overrideReason;

    protected DiscountOverride() {}

    @Default
    protected DiscountOverride(BigDecimal discount, OverrideReason overrideReason) {
        this.discount = discount;
        this.overrideReason = overrideReason;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public OverrideReason getOverrideReason() {
        return overrideReason;
    }
}