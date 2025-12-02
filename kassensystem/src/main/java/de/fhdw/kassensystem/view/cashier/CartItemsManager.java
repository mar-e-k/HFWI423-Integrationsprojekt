package de.fhdw.kassensystem.view.cashier;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import de.fhdw.commons.api.dto.DepositStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CartItemsManager { //This whole construct runs in in-memory. on application restart, all data is lost. if requested, use an external db for this like redis

    private final Map<String, List<CartItem>> carts = new ConcurrentHashMap<>();

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
            return false; // Ein leerer Warenkorb ist kein reiner Pfand-Warenkorb
        }
        return cart.stream().allMatch(item -> item.getDepositStatus() == DepositStatus.EMPTY);
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "guest"; // fallback for anonymous session
        }
        return auth.getName();
    }
}
