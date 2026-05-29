package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderRequestDTO;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderResponseDTO;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderDTO;
import de.fhdw.vendix.commons.spring.web.api.store.StoreStockOrderApi;
import de.fhdw.vendix.store.core.domain.store_stock.StoreStockMapper;
import de.fhdw.vendix.store.core.domain.store_stock_order.StoreStockOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class StoreStockOrderController implements StoreStockOrderApi {

    private final StoreStockOrderService storeStockOrderService;
    private final StoreStockMapper storeStockMapper;

    StoreStockOrderController(
            StoreStockOrderService storeStockOrderService,
            StoreStockMapper storeStockMapper
    ) {
        this.storeStockOrderService = storeStockOrderService;
        this.storeStockMapper = storeStockMapper;
    }

    @Override
    public ResponseEntity<StoreStockOrderDTO> getStoreStockOrderStatus(UUID correlationId) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<StoreStockOrderResponseDTO> createStoreStockOrder(StoreStockOrderRequestDTO request) {
        return ResponseEntity.noContent().build();
    }
}