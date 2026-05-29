package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderRequestDTO;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderResponseDTO;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderDTO;
import de.fhdw.vendix.store.core.domain.store_stock_order.StoreStockOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

//TODO("extract api from branch")

@RestController
class ReplenishmentOrderController {

    private final StoreStockOrderService storeStockOrderService;

    ReplenishmentOrderController(StoreStockOrderService storeStockOrderService) {
        this.storeStockOrderService = storeStockOrderService;
    }

    public ResponseEntity<StoreStockOrderResponseDTO> createReplenishmentOrder(
            StoreStockOrderRequestDTO storeStockOrderRequestDTO
    ) {
        try {
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(storeStockOrderService.requestReplenishment(storeStockOrderRequestDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    public ResponseEntity<StoreStockOrderDTO> getReplenishmentOrderStatus(UUID correlationId) {
        return storeStockOrderService.findStatus(correlationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}