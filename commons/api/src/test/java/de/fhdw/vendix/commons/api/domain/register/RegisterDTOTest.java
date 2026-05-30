package de.fhdw.vendix.commons.api.domain.register;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterDTOTest {

    @Test
    void acceptsValidRegister() {
        RegisterDTO dto = new RegisterDTO(1L, 2L);

        assertThat(dto.storeId()).isEqualTo(2L);
    }

    @Test
    void rejectsNegativeValues() {
        assertThatThrownBy(() -> new RegisterDTO(-1L, 2L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RegisterDTO(1L, -2L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
