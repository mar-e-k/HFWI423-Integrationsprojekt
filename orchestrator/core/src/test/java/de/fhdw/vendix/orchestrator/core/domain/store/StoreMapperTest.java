package de.fhdw.vendix.orchestrator.core.domain.store;

import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StoreMapperTest {

    private final StoreMapper mapper = new StoreMapperImpl();

    @Test
    void mapsEntityToDto() {
        StoreDTO dto = mapper.toDTO(new Store(1L, "DE", "Hamburg", "Main", "1"));

        assertThat(dto.city()).isEqualTo("Hamburg");
    }

    @Test
    void mapsDtoToEntityAndLists() {
        Store entity = mapper.toEntity(new StoreDTO(2L, "DE", "Berlin", "Side", "2"));

        assertThat(entity.getStreet()).isEqualTo("Side");
        assertThat(mapper.toDTOs(List.of(entity))).hasSize(1);
    }
}
