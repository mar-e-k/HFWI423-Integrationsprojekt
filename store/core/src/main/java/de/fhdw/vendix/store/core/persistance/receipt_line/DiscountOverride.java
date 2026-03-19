package de.fhdw.vendix.store.core.persistance.receipt_line;

import de.fhdw.vendix.commons.api.domain.receipt_line.dto.OverrideReasonEnum;
import de.fhdw.vendix.commons.api.structure.mapper.Default;
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
    private BigDecimal discount;

    @Enumerated(EnumType.STRING)
    private OverrideReasonEnum reason;

    protected DiscountOverride() {}

    @Default
    protected DiscountOverride(BigDecimal discount, OverrideReasonEnum reason) {
        this.discount = discount;
        this.reason = reason;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public OverrideReasonEnum getReason() {
        return reason;
    }
}