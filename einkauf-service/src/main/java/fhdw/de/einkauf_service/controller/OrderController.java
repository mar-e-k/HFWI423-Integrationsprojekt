package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.dto.OrderFilterDTO;
import fhdw.de.einkauf_service.dto.OrderItemRequestDTO;
import fhdw.de.einkauf_service.dto.OrderResponseDTO;
import fhdw.de.einkauf_service.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Bestellungen aus Warenkorb absenden und einsehen")
public class OrderController {

    private final PurchaseOrderService purchaseOrderService;

    public OrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping("/submit")
    @Operation(summary = "Bestellung(en) aus aktuellem Warenkorb auslösen",
            description = "Erzeugt pro Lieferant eine separate Bestellung aus dem Session-Warenkorb.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bestellungen angelegt"),
            @ApiResponse(responseCode = "404", description = "Artikel nicht gefunden (ProblemDetail)"),
            @ApiResponse(responseCode = "409", description = "Warenkorb leer / Artikel nicht verfügbar (ProblemDetail)")
    })
    public ResponseEntity<List<OrderResponseDTO>> submitOrderFromCart() {
        List<OrderResponseDTO> responses = purchaseOrderService.createAndSendOrdersFromCart();
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping
    @Operation(summary = "Bestellhistorie filtern",
            description = "Filter über OrderFilterDTO als Query-Parameter (z.B. Lieferant, Status, Zeitraum).")
    @ApiResponse(responseCode = "200", description = "Trefferliste")
    public ResponseEntity<List<OrderResponseDTO>> getOrderHistory(@ModelAttribute OrderFilterDTO filter) {
        return ResponseEntity.ok(purchaseOrderService.getOrderHistory(filter));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Bestelldetails abrufen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bestellung gefunden"),
            @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden (ProblemDetail)")
    })
    public ResponseEntity<OrderResponseDTO> getOrderDetails(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getOrderDetails(id));
    }

    @PostMapping("/{id}/reorder")
    @Operation(summary = "Bestellung erneut auslösen",
            description = "Optional kann eine Liste angepasster OrderItems mitgegeben werden.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Neue Bestellung angelegt"),
            @ApiResponse(responseCode = "404", description = "Originalbestellung nicht gefunden (ProblemDetail)"),
            @ApiResponse(responseCode = "409", description = "Keine gültigen Positionen / Lieferant nicht aktiv (ProblemDetail)")
    })
    public ResponseEntity<OrderResponseDTO> reorderOrder(@PathVariable Long id,
            @RequestBody List<OrderItemRequestDTO> items) {
        OrderResponseDTO newOrder = purchaseOrderService.reorder(id, items);
        return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);
    }
}
