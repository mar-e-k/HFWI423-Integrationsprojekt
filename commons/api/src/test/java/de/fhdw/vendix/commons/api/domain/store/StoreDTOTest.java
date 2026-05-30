package de.fhdw.vendix.commons.api.domain.store;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoreDTOTest {

    @Test
    void acceptsValidStore() {
        StoreDTO dto = new StoreDTO(1L, "DE", "Hamburg", "Main Street", "12A");

        assertThat(dto.city()).isEqualTo("Hamburg");
    }

    @Test
    void rejectsBlankAddressParts() {
        assertThatThrownBy(() -> new StoreDTO(1L, " ", "Hamburg", "Main Street", "12A"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new StoreDTO(1L, "DE", "", "Main Street", "12A"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new StoreDTO(1L, "DE", "Hamburg", "Main Street", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
