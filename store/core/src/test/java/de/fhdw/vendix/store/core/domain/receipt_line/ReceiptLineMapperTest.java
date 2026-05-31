package de.fhdw.vendix.store.core.domain.receipt_line;

import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.embeddable.DiscountOverrideDTO;
import de.fhdw.vendix.commons.api.embeddable.OverrideReason;
import de.fhdw.vendix.commons.api.embeddable.PriceOverrideDTO;
import de.fhdw.vendix.store.core.embeddable.discount_override.DiscountOverrideMapperImpl;
import de.fhdw.vendix.store.core.embeddable.price_override.PriceOverrideMapperImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptLineMapperTest {

    private final ReceiptLineMapper mapper = new ReceiptLineMapperImpl(
            new DiscountOverrideMapperImpl(),
            new PriceOverrideMapperImpl()
    );

    @Test
    void mapsDtoToEntityWithOverrides() {
        ReceiptLineDTO dto = new ReceiptLineDTO(1L, 2L, 3L, 4L,
                new DiscountOverrideDTO(BigDecimal.TEN, OverrideReason.PROMOTIONAL_ADJUSTMENT),
                new PriceOverrideDTO(BigDecimal.valueOf(7), OverrideReason.SYSTEM_CORRECTION));

        ReceiptLine entity = mapper.toEntity(dto);

        assertThat(entity.getReceiptId()).isEqualTo(2L);
        assertThat(entity.getDiscountOverride()).isNotNull();
        assertThat(entity.getPriceOverride()).isNotNull();
    }

    @Test
    void mapsEntityToDtoAndLists() {
        ReceiptLine entity = new ReceiptLine(1L, 2L, 3L);

        assertThat(mapper.toDTO(entity).articleAmount()).isEqualTo(3L);
        assertThat(mapper.toDTOs(List.of(entity))).hasSize(1);
    }
}
