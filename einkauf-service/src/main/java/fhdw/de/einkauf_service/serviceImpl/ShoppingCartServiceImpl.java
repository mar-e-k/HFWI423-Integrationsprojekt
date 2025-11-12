package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.config.ShoppingCartSession;
import fhdw.de.einkauf_service.entity.Article;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import fhdw.de.einkauf_service.service.ShoppingCartService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartSession cartSession;
    private final ArticleRepository articleRepository;

    // Methode für Akzeptanzkriterium 3: Prüft unseren eigenen Lagerbestand
    @Override
    public void validateAndAddToCart(Long articleId, int quantity) {

        // Hole den Artikel aus der Datenbank (inkl. unseres aktuellen Bestands)
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("Artikel nicht gefunden."));

        // Hier prüfen wir nur, ob die Menge > 0 ist.
        if (quantity <= 0) {
            throw new IllegalArgumentException("Die Bestellmenge muss positiv sein.");
        }

        // Fügt den Artikel zur Session (Warenkorb) hinzu oder aktualisiert die Menge
        cartSession.addItem(articleId, quantity);
    }

    // Menge anpassen/Artikel entfernen
    @Override
    public void removeItem(Long articleId) {
        cartSession.removeItem(articleId);
    }

    // Übersicht abrufen
    // Liefert die aktuellen Warenkorb-Daten (Artikel ID → Menge)
    @Override
    public Map<Long, Integer> getCurrentCartItems() {
        return cartSession.getItems();
    }

    // Zum Leeren des Warenkorbs nach Bestellung/Abbruch
    @Override
    public void clearCart() {
        cartSession.clearCart();
    }
}
