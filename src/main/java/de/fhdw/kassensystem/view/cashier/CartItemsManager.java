package de.fhdw.kassensystem.view.cashier;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "guest"; // fallback for anonymous session
        }
        return auth.getName();
    }
}