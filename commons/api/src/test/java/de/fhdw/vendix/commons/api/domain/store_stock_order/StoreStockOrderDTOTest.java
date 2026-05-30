package de.fhdw.vendix.commons.api.domain.store_stock_order;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NullAway")
class StoreStockOrderDTOTest {

    private static final UUID CORRELATION_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");

    @Test
    void requestDefaultsNullUrgentToFalse() {
        StoreStockOrderRequestDTO request = new StoreStockOrderRequestDTO(1L, 2L, 3L, null);

        assertThat(request.urgent()).isFalse();
    }

    @Test
    void requestRejectsInvalidIdsAndAmount() {
        assertThatThrownBy(() -> new StoreStockOrderRequestDTO(0L, 2L, 3L, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new StoreStockOrderRequestDTO(1L, 0L, 3L, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new StoreStockOrderRequestDTO(1L, 2L, 0L, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void responseRequiresCorrelationStatusAndStatusUrl() {
        StoreStockOrderResponseDTO response = new StoreStockOrderResponseDTO(CORRELATION_ID, OrderStatus.ORDERED,
                "/api/store-stock-order/correlation-id/" + CORRELATION_ID);

        assertThat(response.status()).isEqualTo(OrderStatus.ORDERED);
        assertThatThrownBy(() -> new StoreStockOrderResponseDTO(null, OrderStatus.ORDERED, "/status"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new StoreStockOrderResponseDTO(CORRELATION_ID, null, "/status"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new StoreStockOrderResponseDTO(CORRELATION_ID, OrderStatus.ORDERED, " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void domainDtoRejectsMissingCorrelationOrStatus() {
        assertThatThrownBy(() -> new StoreStockOrderDTO(1L, null, 1L, 2L, 3L, false, OrderStatus.ORDERED, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new StoreStockOrderDTO(1L, CORRELATION_ID, 1L, 2L, 3L, false, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
