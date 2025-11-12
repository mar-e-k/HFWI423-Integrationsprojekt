package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.dto.OrderRequestDTO;
import fhdw.de.einkauf_service.dto.OrderResponseDTO;
import fhdw.de.einkauf_service.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> placeOrder(@RequestBody @Valid OrderRequestDTO request) {

        OrderResponseDTO response = purchaseOrderService.createAndSendOrder(request);

        // Status 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
