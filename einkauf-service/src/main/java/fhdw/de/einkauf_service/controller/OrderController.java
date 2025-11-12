package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.dto.OrderResponseDTO;
import fhdw.de.einkauf_service.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PurchaseOrderService purchaseOrderService;

    // Löst die Bestellung aus dem Warenkorb aus
    @PostMapping("/submit")
    public ResponseEntity<List<OrderResponseDTO>> submitOrderFromCart() {
        List<OrderResponseDTO> responses = purchaseOrderService.createAndSendOrdersFromCart();
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
}
