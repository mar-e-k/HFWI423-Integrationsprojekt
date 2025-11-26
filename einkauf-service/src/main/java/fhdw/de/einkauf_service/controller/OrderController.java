package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.dto.OrderFilterDTO;
import fhdw.de.einkauf_service.dto.OrderItemRequestDTO;
import fhdw.de.einkauf_service.dto.OrderResponseDTO;
import fhdw.de.einkauf_service.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getOrderHistory(@ModelAttribute OrderFilterDTO filter) {
        List<OrderResponseDTO> responses = purchaseOrderService.getOrderHistory(filter);
        return ResponseEntity.ok(responses);
    }

    // Details einer bestimmten Bestellung
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderDetails(@PathVariable Long id) {
        OrderResponseDTO response = purchaseOrderService.getOrderDetails(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reorder")
    public ResponseEntity<OrderResponseDTO> reorderOrder(@PathVariable Long id,
            // Der Body kann eine Liste von Positionen (ggf. mit angepassten Mengen) enthalten
            @RequestBody List<OrderItemRequestDTO> items) {

        OrderResponseDTO newOrder = purchaseOrderService.reorder(id, items);
        return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);
    }

}
