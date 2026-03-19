package de.fhdw.vendix.store.core.persistance.receipt_line;

import de.fhdw.vendix.commons.api.domain.receipt_line.dto.OverrideReasonEnum;
import de.fhdw.vendix.commons.api.structure.mapper.Default;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

@Embeddable
@SuppressWarnings("NullAway")
public class PriceOverride {

    @DecimalMin(value = "0.00")
    @Column(name = "price_override_amount")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_override_reason")
    private OverrideReasonEnum reason;

    protected PriceOverride() {}

    @Default
    protected PriceOverride(BigDecimal price, OverrideReasonEnum reason) {
        this.price = price;
        this.reason = reason;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public OverrideReasonEnum getReason() {
        return reason;
    }
}