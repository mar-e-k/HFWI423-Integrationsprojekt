package fhdw.de.einkauf_service.config;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@SessionScope
public class ShoppingCartSession {

    // Die Map speichert die Artikel-ID als Key und die bestellte Menge als Value
    private final Map<Long, Integer> items = new HashMap<>();

    public void addItem(Long articleId, int quantity) {
        // Logik: Fügt hinzu oder aktualisiert die Menge
        items.merge(articleId, quantity, Integer::sum);
        // Oder: item.put(articleId, quantity) für eine direkte Überschreibung
    }

    public void removeItem(Long articleId) {
        items.remove(articleId);
    }

    public Map<Long, Integer> getItems() {
        return Collections.unmodifiableMap(items);
    }

    public void clearCart() {
        items.clear();
    }
}
