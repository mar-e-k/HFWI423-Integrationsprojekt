package de.fhdw.vendix.commons.api.embeddable;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NullAway")
class OverrideDTOTest {

    @Test
    void discountOverrideAcceptsPercentageRange() {
        DiscountOverrideDTO dto = new DiscountOverrideDTO(BigDecimal.valueOf(25), OverrideReason.PROMOTIONAL_ADJUSTMENT);

        assertThat(dto.amount()).isEqualByComparingTo("25");
    }

    @Test
    void discountOverrideRejectsInvalidRangeOrMissingReason() {
        assertThatThrownBy(() -> new DiscountOverrideDTO(BigDecimal.valueOf(-1), OverrideReason.OTHER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DiscountOverrideDTO(BigDecimal.valueOf(101), OverrideReason.OTHER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DiscountOverrideDTO(BigDecimal.TEN, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void priceOverrideRejectsNegativeAmount() {
        assertThat(new PriceOverrideDTO(BigDecimal.valueOf(5), OverrideReason.SYSTEM_CORRECTION).amount())
                .isEqualByComparingTo("5");
        assertThatThrownBy(() -> new PriceOverrideDTO(BigDecimal.valueOf(-1), OverrideReason.SYSTEM_CORRECTION))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
