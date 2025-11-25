package de.fhdw.kassensystem.view.cashier;

import de.fhdw.kassensystem.persistence.entity.imported.Article;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {

    /**
     * SCRUM-58 – Rabattfunktion (z.B. „30 %“):
     * Prüft, dass getDiscountedUnitPrice den Basispreis korrekt um den
     * konfigurierten prozentualen Rabatt reduziert.
     */
    @Test
    void discountedUnitPrice_isReducedByGivenPercent() {
        Article article = new Article();
        article.setSellingPrice(100.00);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

        CartItem item = new CartItem(article, 1, 1, null);
        item.setDiscountPercent(new BigDecimal("30"));
        item.setDiscountedQuantity(1);

        BigDecimal discountedUnitPrice = item.getDiscountedUnitPrice();
        BigDecimal expected = new BigDecimal("70.00");

        assertEquals(0, expected.compareTo(discountedUnitPrice),
                "Rabattierter Stückpreis (30 %) ist nicht korrekt berechnet.");
    }

    /**
     * SCRUM-58 – Rabattfunktion (z.B. „30 %“):
     * Stellt sicher, dass der Rabatt nur auf die konfigurierte rabattierte Menge angewendet
     * wird und der Gesamtpreis entsprechend Teilrabatt + Vollpreis korrekt berechnet wird.
     */
    @Test
    void totalPriceWithDiscount_appliesPartialDiscountOnlyToDiscountedQuantity() {
        Article article = new Article();
        article.setSellingPrice(10.00);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

        CartItem item = new CartItem(article, 1, 5, null);
        item.setDiscountPercent(new BigDecimal("30"));
        item.setDiscountedQuantity(2);

        BigDecimal expected = new BigDecimal("44.00");
        BigDecimal total = item.getTotalPriceWithDiscount();

        assertEquals(0, expected.compareTo(total),
                "Teilrabatt (nur eine Teilmenge rabattiert) wird nicht korrekt berechnet.");
    }

    /**
     * SCRUM-58 – Rabattfunktion (z.B. „30 %“):
     * Prüft, dass hasDiscount() nur dann true ist, wenn sowohl ein positiver Prozentwert
     * als auch eine positive rabattierte Menge gesetzt sind.
     */
    @Test
    void hasDiscount_isFalseWhenPercentOrQuantityInvalid() {
        Article article = new Article();
        article.setSellingPrice(10.00);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

        CartItem item = new CartItem(article, 1, 1, null);

        assertFalse(item.hasDiscount(), "Ohne Prozent und Menge darf kein Rabatt aktiv sein.");

        item.setDiscountPercent(BigDecimal.ZERO);
        item.setDiscountedQuantity(1);
        assertFalse(item.hasDiscount(), "Rabatt 0 % darf nicht als aktiver Rabatt gewertet werden.");

        item.setDiscountPercent(new BigDecimal("10"));
        item.setDiscountedQuantity(0);
        assertFalse(item.hasDiscount(), "Rabatt mit Menge 0 darf nicht als aktiver Rabatt gewertet werden.");
    }

    /**
     * SCRUM-65 – Manuelle Preisänderung bei fehlendem Verkaufspreis:
     * Verifiziert, dass ein explizit gesetzter Overridden-Preis als Basispreis verwendet wird
     * und damit die manuelle Preiseingabe für einen Artikel unterstützt.
     */
    @Test
    void baseUnitPrice_prefersOverriddenPriceOverArticlePrice() {
        Article article = new Article();
        article.setSellingPrice(15.00);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

        CartItem item = new CartItem(article, 1, 1, null);
        item.setOverriddenPrice(new BigDecimal("12.34"));

        BigDecimal basePrice = item.getBaseUnitPrice();
        BigDecimal expected = new BigDecimal("12.34");

        assertEquals(0, expected.compareTo(basePrice),
                "Overridden Price muss Vorrang vor dem im Artikel hinterlegten Verkaufspreis haben.");
    }

    /**
     * SCRUM-57 – Kassenbon-Übersicht:
     * Stellt sicher, dass der Konstruktor Position und Menge korrekt zuweist, damit die
     * Reihenfolge und Anzahl der Artikel im Warenkorb und auf dem Bon konsistent bleibt.
     *
     * Hintergrund: In einer früheren Version waren position und quantity im Konstruktor
     * vertauscht, was zu falschen Bonpositionen führte.
     */
    @Test
    void constructor_assignsPositionAndQuantityCorrectly() {
        Article article = new Article();
        article.setName("Kontr-Test");
        article.setSellingPrice(1.00);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

        CartItem item = new CartItem(article, 5, 3, null);

        assertAll(
                () -> assertEquals(5, item.getPosition(),
                        "Position im Warenkorb muss korrekt gesetzt werden."),
                () -> assertEquals(3, item.getQuantity(),
                        "Quantity muss korrekt gesetzt werden.")
        );
    }
}