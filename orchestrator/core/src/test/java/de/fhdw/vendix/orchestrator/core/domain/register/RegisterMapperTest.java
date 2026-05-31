package de.fhdw.vendix.orchestrator.core.domain.register;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterMapperTest {

    private final RegisterMapper mapper = new RegisterMapperImpl();

    @Test
    void mapsEntityToDto() {
        RegisterDTO dto = mapper.toDTO(new Register(1L, 2L));

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.storeId()).isEqualTo(2L);
    }

    @Test
    void mapsDtoToEntityAndLists() {
        Register entity = mapper.toEntity(new RegisterDTO(3L, 4L));

        assertThat(entity.getStoreId()).isEqualTo(4L);
        assertThat(mapper.toDTOs(List.of(entity))).hasSize(1);
    }
}
