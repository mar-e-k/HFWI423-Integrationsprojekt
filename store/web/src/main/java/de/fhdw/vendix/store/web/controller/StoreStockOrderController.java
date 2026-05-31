package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderRequestDTO;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderResponseDTO;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderDTO;
import de.fhdw.vendix.commons.spring.web.api.store.StoreStockOrderApi;
import de.fhdw.vendix.store.core.domain.store_stock_order.StoreStockOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class StoreStockOrderController implements StoreStockOrderApi {

    private final StoreStockOrderService storeStockOrderService;

    StoreStockOrderController(StoreStockOrderService storeStockOrderService) {
        this.storeStockOrderService = storeStockOrderService;
    }

    @Override
    public ResponseEntity<StoreStockOrderDTO> getStoreStockOrderStatus(UUID correlationId) {
        return storeStockOrderService.findStatus(correlationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<StoreStockOrderResponseDTO> createStoreStockOrder(StoreStockOrderRequestDTO request) {
        StoreStockOrderResponseDTO response = storeStockOrderService.requestReplenishment(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
