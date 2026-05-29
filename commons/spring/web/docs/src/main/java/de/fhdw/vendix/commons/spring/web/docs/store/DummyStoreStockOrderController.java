package de.fhdw.vendix.commons.spring.web.docs.store;

import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderDTO;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderRequestDTO;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderResponseDTO;
import de.fhdw.vendix.commons.spring.web.api.store.StoreStockOrderApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class DummyStoreStockOrderController implements StoreStockOrderApi {

    @Override
    public ResponseEntity<StoreStockOrderDTO> getStoreStockOrderStatus(UUID correlationId) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<StoreStockOrderResponseDTO> createStoreStockOrder(StoreStockOrderRequestDTO request) {
        return ResponseEntity.noContent().build();
    }
}
