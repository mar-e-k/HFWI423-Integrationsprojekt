package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.config.ShoppingCartSession;
import fhdw.de.einkauf_service.entity.Article;
import fhdw.de.einkauf_service.metrics.MetricsRegistry;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import fhdw.de.einkauf_service.service.ShoppingCartService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartSession cartSession;
    private final ArticleRepository articleRepository;
    private final MetricsRegistry metrics;

    public ShoppingCartServiceImpl(ShoppingCartSession cartSession, ArticleRepository articleRepository, MetricsRegistry metrics) {
        this.cartSession = cartSession;
        this.articleRepository = articleRepository;
        this.metrics = metrics;
    }

    @Override
    public void validateAndAddToCart(Long articleId, int quantity) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("Artikel nicht gefunden."));

        if (quantity <= 0) {
            throw new IllegalArgumentException("Die Bestellmenge muss positiv sein.");
        }

        cartSession.addItem(articleId, quantity);

        // 📊 TRACKING: Artikel zum Warenkorb hinzugefügt
        metrics.cartItemsAdded.increment();
    }

    @Override
    public void removeItem(Long articleId) {
        cartSession.removeItem(articleId);

        // 📊 TRACKING: Artikel aus Warenkorb entfernt
        metrics.cartItemsRemoved.increment();
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
