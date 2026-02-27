package de.fhdw.vendix.pos.view.cashier;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import de.fhdw.vendix.commons.core.api.dto.DepositStatus;
import de.fhdw.vendix.commons.security.spring.AuthContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CartItemsManager {

    private final Map<String, List<CartItem>> carts = new ConcurrentHashMap<>();

    public CartItemsManager() {}

    public List<CartItem> getCart() {
        return carts.computeIfAbsent(currentUsername(), u -> Collections.synchronizedList(new ArrayList<>()));
    }

    public void clearCart() {
        carts.remove(currentUsername());
    }

    public void updateGrid(Grid<CartItem> grid, Span totalLabel) {
        List<CartItem> items = new ArrayList<>(getCart());
        items.sort(Comparator.comparingInt(CartItem::getPosition));

        int pos = 1;
        for (CartItem item : items) {
            item.setPosition(pos++);
        }

        grid.setItems(items);
        grid.getDataProvider().refreshAll();

        int totalQuantity = items.stream().mapToInt(CartItem::getQuantity).sum();
        BigDecimal totalPrice = items.stream()
                .map(CartItem::getTotalPriceWithDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalLabel.setText(String.format("Gesamtanzahl: %d | Gesamtpreis: %s €",
                totalQuantity, totalPrice.toPlainString()));
    }

    public boolean isDepositOnly() {
        List<CartItem> cart = getCart();
        if (cart.isEmpty()) {
            return false;
        }
        return cart.stream().allMatch(item -> item.getDepositStatus() == DepositStatus.EMPTY);
    }

    private String currentUsername() {
        return AuthContextHolder.current().map(AuthContext::getName).orElseThrow(IllegalStateException::new);
    }
}
