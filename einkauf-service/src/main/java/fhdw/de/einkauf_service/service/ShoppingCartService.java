package fhdw.de.einkauf_service.service;

import java.util.Map;

public interface ShoppingCartService {
    void validateAndAddToCart(Long articleId, int quantity);
    Map<Long, Integer> getCurrentCartItems();
    void clearCart();
    void removeItem(Long articleId);
}
