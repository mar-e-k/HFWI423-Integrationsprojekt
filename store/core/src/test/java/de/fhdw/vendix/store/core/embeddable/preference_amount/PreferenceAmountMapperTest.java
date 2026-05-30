package de.fhdw.vendix.store.core.embeddable.preference_amount;

import de.fhdw.vendix.commons.api.embeddable.PreferenceAmountDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PreferenceAmountMapperTest {

    private final PreferenceAmountMapper mapper = new PreferenceAmountMapperImpl();

    @Test
    void mapsEntityToDto() {
        PreferenceAmountDTO dto = mapper.toDTO(new PreferenceAmount(1L, 5L, 10L));

        assertThat(dto.min()).isEqualTo(1L);
        assertThat(dto.avg()).isEqualTo(5L);
        assertThat(dto.max()).isEqualTo(10L);
    }

    @Test
    void mapsDtoToEntityAndLists() {
        PreferenceAmount entity = mapper.toEntity(new PreferenceAmountDTO(2L, 6L, 12L));

        assertThat(entity.getMax()).isEqualTo(12L);
        assertThat(mapper.toDTOs(List.of(entity))).hasSize(1);
    }
}
