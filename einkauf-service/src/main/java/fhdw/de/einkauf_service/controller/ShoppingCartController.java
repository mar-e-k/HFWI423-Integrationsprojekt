package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Shopping Cart", description = "Session-basierter Warenkorb")
public class ShoppingCartController {

    private final ShoppingCartService cartService;

    public ShoppingCartController(ShoppingCartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    @Operation(summary = "Artikel zum Warenkorb hinzufügen oder Menge ändern")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Warenkorb aktualisiert"),
            @ApiResponse(responseCode = "400", description = "Ungültige Menge (ProblemDetail)")
    })
    public ResponseEntity<Void> addItem(@RequestParam Long articleId, @RequestParam int quantity) {
        cartService.validateAndAddToCart(articleId, quantity);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Aktuellen Warenkorb anzeigen",
            description = "Liefert eine Map von Artikel-ID auf Menge.")
    @ApiResponse(responseCode = "200", description = "Aktueller Warenkorb")
    public Map<Long, Integer> getCart() {
        return cartService.getCurrentCartItems();
    }
}
