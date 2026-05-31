package de.fhdw.vendix.store.core.embeddable.price_override;

import de.fhdw.vendix.commons.api.embeddable.OverrideReason;
import de.fhdw.vendix.commons.api.embeddable.PriceOverrideDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PriceOverrideMapperTest {

    private final PriceOverrideMapper mapper = new PriceOverrideMapperImpl();

    @Test
    void mapsEntityToDto() {
        PriceOverrideDTO dto = mapper.toDTO(new PriceOverride(BigDecimal.valueOf(7), OverrideReason.SYSTEM_CORRECTION));

        assertThat(dto.amount()).isEqualByComparingTo("7");
        assertThat(dto.reason()).isEqualTo(OverrideReason.SYSTEM_CORRECTION);
    }

    @Test
    void mapsDtoToEntityAndLists() {
        PriceOverride entity = mapper.toEntity(new PriceOverrideDTO(BigDecimal.valueOf(8), OverrideReason.PRICE_NOT_FOUND));

        assertThat(entity.getReason()).isEqualTo(OverrideReason.PRICE_NOT_FOUND);
        assertThat(mapper.toEntities(List.of(new PriceOverrideDTO(BigDecimal.ONE, OverrideReason.OTHER)))).hasSize(1);
    }
}
