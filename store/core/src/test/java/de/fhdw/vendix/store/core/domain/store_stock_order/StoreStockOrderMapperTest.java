package de.fhdw.vendix.store.core.domain.store_stock_order;

import de.fhdw.vendix.commons.api.domain.store_stock_order.OrderStatus;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StoreStockOrderMapperTest {

    private final StoreStockOrderMapper mapper = new StoreStockOrderMapperImpl();

    @Test
    void mapsEntityToDto() {
        UUID correlationId = UUID.fromString("cccccccc-cccc-4ccc-8ccc-cccccccccccc");

        StoreStockOrderDTO dto = mapper.toDTO(new StoreStockOrder(1L, correlationId, 2L, 3L, 4L, true,
                OrderStatus.ORDERED, "published"));

        assertThat(dto.correlationId()).isEqualTo(correlationId);
        assertThat(dto.urgent()).isTrue();
    }

    @Test
    void mapsDtoToEntityAndLists() {
        UUID correlationId = UUID.fromString("dddddddd-dddd-4ddd-8ddd-dddddddddddd");
        StoreStockOrder entity = mapper.toEntity(new StoreStockOrderDTO(1L, correlationId, 2L, 3L, 4L, false,
                OrderStatus.PENDING, "accepted"));

        assertThat(entity.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(mapper.toDTOs(List.of(entity))).hasSize(1);
    }
}
