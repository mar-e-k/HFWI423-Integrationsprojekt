package de.fhdw.vendix.store.core.embeddable.discount_override;

import de.fhdw.vendix.commons.api.embeddable.DiscountOverrideDTO;
import de.fhdw.vendix.commons.api.embeddable.OverrideReason;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountOverrideMapperTest {

    private final DiscountOverrideMapper mapper = new DiscountOverrideMapperImpl();

    @Test
    void mapsEntityToDto() {
        DiscountOverrideDTO dto = mapper.toDTO(new DiscountOverride(BigDecimal.TEN, OverrideReason.OTHER));

        assertThat(dto.amount()).isEqualByComparingTo("10");
        assertThat(dto.reason()).isEqualTo(OverrideReason.OTHER);
    }

    @Test
    void mapsDtoToEntityAndLists() {
        DiscountOverride entity = mapper.toEntity(new DiscountOverrideDTO(BigDecimal.ONE, OverrideReason.CUSTOMER_REQUEST));

        assertThat(entity.getAmount()).isEqualByComparingTo("1");
        assertThat(mapper.toDTOs(List.of(entity))).hasSize(1);
    }
}
