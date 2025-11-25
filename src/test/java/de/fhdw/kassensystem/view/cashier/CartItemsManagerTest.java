package de.fhdw.kassensystem.view.cashier;

import de.fhdw.kassensystem.persistence.entity.imported.Article;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CartItemsManagerTest {

    /**
     * SCRUM-64 – Persistenter Warenkorb:
     * Prüft, dass für verschiedene Benutzer getrennte Warenkörbe verwaltet werden und
     * Änderungen eines Benutzers keinen Einfluss auf den Warenkorb eines anderen Benutzers haben.
     */
    @Test
    void cartIsStoredPerUser_andSeparatedBetweenUsers() {
        CartItemsManager manager = new CartItemsManager();

        setSecurityUser("user1");
        List<CartItem> cartUser1 = manager.getCart();
        cartUser1.add(dummyCartItem("User1-Artikel"));

        setSecurityUser("user2");
        List<CartItem> cartUser2 = manager.getCart();

        assertAll(
                () -> assertNotSame(cartUser1, cartUser2, "Jeder Benutzer muss eine eigene Warenkorb-Liste erhalten."),
                () -> assertEquals(1, cartUser1.size(), "Warenkorb von user1 sollte 1 Artikel enthalten."),
                () -> assertEquals(0, cartUser2.size(), "Warenkorb von user2 muss unabhängig von user1 sein.")
        );

        cartUser2.add(dummyCartItem("User2-Artikel"));

        setSecurityUser("user1");
        List<CartItem> cartUser1Again = manager.getCart();
        setSecurityUser("user2");
        List<CartItem> cartUser2Again = manager.getCart();

        assertAll(
                () -> assertEquals(1, cartUser1Again.size(),
                        "Warenkorb von user1 darf durch Änderungen an user2 nicht verändert werden."),
                () -> assertEquals(1, cartUser2Again.size(),
                        "Warenkorb von user2 sollte 1 Artikel enthalten.")
        );
    }

    /**
     * SCRUM-64 – Persistenter Warenkorb:
     * Stellt sicher, dass die Warenkorbdaten für denselben Benutzer über mehrere
     * Zugriffe hinweg erhalten bleiben – der Warenkorb wird „persistiert“.
     */
    @Test
    void cartPersistsForSameUserAcrossMultipleAccesses() {
        CartItemsManager manager = new CartItemsManager();

        setSecurityUser("user-persist");
        List<CartItem> firstAccess = manager.getCart();
        CartItem item = dummyCartItem("Persist-Artikel");
        firstAccess.add(item);

        List<CartItem> secondAccess = manager.getCart();

        assertAll(
                () -> assertSame(firstAccess, secondAccess,
                        "Für denselben Benutzer sollte dieselbe Warenkorb-Liste zurückgegeben werden."),
                () -> assertEquals(1, secondAccess.size(),
                        "Warenkorb des gleichen Benutzers muss über mehrere Zugriffe hinweg erhalten bleiben."),
                () -> assertTrue(secondAccess.contains(item),
                        "Der hinzugefügte Artikel muss auch beim zweiten Zugriff noch im Warenkorb liegen.")
        );
    }

    /**
     * SCRUM-64 – Persistenter Warenkorb:
     * Prüft, dass clearCart() nur den Warenkorb des aktuell eingeloggten Benutzers leert
     * und andere Benutzer-Warenkörbe unverändert bleiben.
     */
    @Test
    void clearCart_emptiesOnlyCurrentUsersCart() {
        CartItemsManager manager = new CartItemsManager();

        setSecurityUser("closer");
        List<CartItem> cartA = manager.getCart();
        cartA.add(dummyCartItem("Artikel A"));

        setSecurityUser("other");
        List<CartItem> cartB = manager.getCart();
        cartB.add(dummyCartItem("Artikel B"));

        setSecurityUser("closer");
        manager.clearCart();

        setSecurityUser("closer");
        assertEquals(0, manager.getCart().size(),
                "Nach clearCart() muss der Warenkorb des aktuellen Benutzers leer sein.");
        setSecurityUser("other");
        assertEquals(1, manager.getCart().size(),
                "clearCart() darf nicht den Warenkorb anderer Benutzer löschen.");
    }

    // Hilfsmethoden

    private void setSecurityUser(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                username,
                "password",
                List.of(() -> "ROLE_USER")
        );
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    private CartItem dummyCartItem(String name) {
        Article article = new Article();
        article.setName(name);
        article.setSellingPrice(1.00);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);
        return new CartItem(article, 1, 1, null);
    }
}