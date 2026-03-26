package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.service.PurchaseOrderService;
import fhdw.de.einkauf_service.service.ShoppingCartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
public class ShoppingCartController {

    private final ShoppingCartService cartService;

    public ShoppingCartController(ShoppingCartService cartService) {
        this.cartService = cartService;
    }

    // Fügt einen Artikel zum Session-Warenkorb hinzu/ändert die Menge
    @PostMapping("/add")
    public ResponseEntity<Void> addItem(@RequestParam Long articleId, @RequestParam int quantity) {
        cartService.validateAndAddToCart(articleId, quantity);
        return ResponseEntity.ok().build();
    }

    // Zeigt den aktuellen Warenkorb an (für die Kontrolle)
    @GetMapping
    public Map<Long, Integer> getCart() {
        return cartService.getCurrentCartItems();
    }
}
