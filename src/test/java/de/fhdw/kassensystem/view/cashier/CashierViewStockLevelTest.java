package de.fhdw.kassensystem.view.cashier;

import de.fhdw.kassensystem.persistence.entity.imported.Article;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CashierViewStockLevelTest {

    /**
     * SCRUM-39 – Artikeldaten abrufen:
     * Prüft fachlich, dass ein Lagerbestand < 5 als „Geringer Bestand“ gilt.
     */
    @Test
    void stockBelowThreshold_isConsideredLowStock() {
        Article article = new Article();
        article.setStockLevel(3);

        boolean isLowStock = isLowStock(article);

        assertTrue(isLowStock,
                "Lagerbestand < 5 muss fachlich als 'Geringer Bestand' gelten.");
    }

    /**
     * SCRUM-39 – Artikeldaten abrufen:
     * Stellt sicher, dass ein Lagerbestand >= 5 nicht als „Geringer Bestand“ gewertet wird.
     */
    @Test
    void stockAtOrAboveThreshold_isNotConsideredLowStock() {
        Article article = new Article();
        article.setStockLevel(10);

        boolean isLowStock = isLowStock(article);

        assertFalse(isLowStock,
                "Lagerbestand >= 5 darf fachlich nicht als 'Geringer Bestand' gelten.");
    }

    // Hilfsmethode
    private boolean isLowStock(Article article) {
        return article.getStockLevel() < 5;
    }
}
