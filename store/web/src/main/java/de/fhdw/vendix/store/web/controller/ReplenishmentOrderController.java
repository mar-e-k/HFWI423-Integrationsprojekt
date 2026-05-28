package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.replenishment.ReplenishmentOrderRequestDTO;
import de.fhdw.vendix.commons.api.domain.replenishment.ReplenishmentOrderResponseDTO;
import de.fhdw.vendix.commons.api.domain.replenishment.ReplenishmentOrderStatusDTO;
import de.fhdw.vendix.store.core.domain.replenishment.ReplenishmentOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

//TODO("extract api from branch")

@RestController
class ReplenishmentOrderController {

    private final ReplenishmentOrderService replenishmentOrderService;

    ReplenishmentOrderController(ReplenishmentOrderService replenishmentOrderService) {
        this.replenishmentOrderService = replenishmentOrderService;
    }

    public ResponseEntity<ReplenishmentOrderResponseDTO> createReplenishmentOrder(
            ReplenishmentOrderRequestDTO replenishmentOrderRequestDTO
    ) {
        try {
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(replenishmentOrderService.requestReplenishment(replenishmentOrderRequestDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    public ResponseEntity<ReplenishmentOrderStatusDTO> getReplenishmentOrderStatus(UUID correlationId) {
        return replenishmentOrderService.findStatus(correlationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}