package de.fhdw.vendix.commons.api.embeddable;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PreferenceAmountDTOTest {

    @Test
    void acceptsOrderedAmounts() {
        PreferenceAmountDTO dto = new PreferenceAmountDTO(1L, 5L, 10L);

        assertThat(dto.avg()).isEqualTo(5L);
    }

    @Test
    void rejectsNegativeOrUnorderedAmounts() {
        assertThatThrownBy(() -> new PreferenceAmountDTO(-1L, 5L, 10L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PreferenceAmountDTO(6L, 5L, 10L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PreferenceAmountDTO(1L, 11L, 10L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
