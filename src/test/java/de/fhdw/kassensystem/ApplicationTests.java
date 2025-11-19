package de.fhdw.kassensystem;

import de.fhdw.kassensystem.persistence.entity.Account;
import de.fhdw.kassensystem.persistence.entity.AccountRole;
import de.fhdw.kassensystem.persistence.entity.AccountRoleEnum;
import de.fhdw.kassensystem.persistence.entity.imported.Article;
import de.fhdw.kassensystem.persistence.repository.AccountRoleRepository;
import de.fhdw.kassensystem.persistence.repository.ArticleRepository;
import de.fhdw.kassensystem.persistence.service.AccountRoleService;
import de.fhdw.kassensystem.persistence.service.ArticleService;
import de.fhdw.kassensystem.persistence.service.ReceiptService;
import de.fhdw.kassensystem.view.admin.RoleView;
import de.fhdw.kassensystem.view.cashier.CartItem;
import de.fhdw.kassensystem.view.cashier.CartItemsManager;
import de.fhdw.kassensystem.view.cashier.CashierView;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Zentrale Testklasse - Tests sind auf Unit-Ebene
 */
@SpringBootTest
class ApplicationTests {

    @Test
    void contextLoads() {
    }

    /**
     * Beleg enthält: Datum, Uhrzeit, Artikel, Einzelpreise, Rabatt, Steuersatz, Zahlungsart, Gesamtbetrag.
     */
    @Test
    void receiptContainsMandatoryFields_forCashPayment() throws Exception {
        Article article = new Article();
        article.setName("Test-Artikel");
        article.setSellingPrice(9.99);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

        // position = 1, quantity = 2, kein Override-Preis
        CartItem cartItem = new CartItem(article, 1, 2, null);
        ReceiptService receiptService = new ReceiptService();

        ByteArrayInputStream pdfStream = receiptService.generateReceipt(List.of(cartItem), true);
        byte[] pdfBytes = pdfStream.readAllBytes();
        String text;

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(document);
        }

        BigDecimal expectedTotal = cartItem.getTotalPriceWithDiscount();
        String expectedTotalText = String.format("Gesamtbetrag: %.2f EUR", expectedTotal.doubleValue());

        // *** WICHTIG: Format so wie im ReceiptService ***
        String expectedUnitPriceText =
                String.format("%.2f EUR", cartItem.getBaseUnitPrice().doubleValue());

        String expectedTaxRateText =
                String.format("%d %%", article.getTaxRatePercent().intValue());

        String expectedDiscountText =
                String.format("%d %% x %d",
                        cartItem.getDiscountPercent().intValue(),
                        cartItem.getDiscountedQuantity());

        assertAll(
                // Beleg-Titel & Datum/Uhrzeit
                () -> assertTrue(text.contains("Kaufbeleg"), "Beleg-Titel (Kaufbeleg) fehlt."),
                () -> assertTrue(text.contains("Datum:"), "Datum/Uhrzeit wird nicht ausgegeben."),

                // Artikel
                () -> assertTrue(text.contains("Test-Artikel"), "Artikelname fehlt im Beleg."),

                // Einzelpreis (ohne Label, nur z.B. '9.99 EUR')
                () -> assertTrue(text.contains(expectedUnitPriceText),
                        "Einzelpreis wird nicht korrekt angezeigt."),

                // Rabatt (z.B. '0 % x 0' bei Standardwerten)
                () -> assertTrue(text.contains(expectedDiscountText),
                        "Rabatt wird nicht korrekt angezeigt."),

                // Steuersatz (z.B. '19 %')
                () -> assertTrue(text.contains(expectedTaxRateText),
                        "Steuersatz wird nicht korrekt angezeigt."),

                // Gesamtbetrag
                () -> assertTrue(text.contains(expectedTotalText),
                        "Gesamtbetrag wird nicht korrekt angezeigt."),

                // Zahlungsart
                () -> assertTrue(text.contains("Bargeldzahlung"),
                        "Zahlungsart 'Bargeldzahlung' wird nicht angezeigt.")
        );
    }

    /**
     * Das Datum enthält auch eine Uhrzeit im erwarteten Format (dd.MM.yyyy HH:mm:ss).
     */
    @Test
    void receiptContainsFormattedDateTime() throws Exception {
        Article article = new Article();
        article.setName("Zeit-Artikel");
        article.setSellingPrice(1.00);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

        CartItem cartItem = new CartItem(article, 1, 1, null);
        ReceiptService receiptService = new ReceiptService();

        ByteArrayInputStream pdfStream = receiptService.generateReceipt(List.of(cartItem), true);
        byte[] pdfBytes = pdfStream.readAllBytes();
        String text;

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(document);
        }

        Pattern dateTimePattern = Pattern.compile("Datum:\\s*\\d{2}\\.\\d{2}\\.\\d{4}\\s+\\d{2}:\\d{2}:\\d{2}");
        assertTrue(dateTimePattern.matcher(text).find(),
                "Datum/Uhrzeit im Beleg entspricht nicht dem erwarteten Format dd.MM.yyyy HH:mm:ss.");
    }

    /**
     * Zeilen mit Steuersatz und Einzelpreis werden im PDF korrekt ausgegeben.
     */
    @Test
    void receiptContainsTaxRateAndUnitPrice() throws Exception {
        Article article = new Article();
        article.setName("Steuerartikel");
        article.setSellingPrice(5.50);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

        CartItem cartItem = new CartItem(article, 1, 1, null);
        ReceiptService receiptService = new ReceiptService();

        ByteArrayInputStream pdfStream = receiptService.generateReceipt(List.of(cartItem), true);
        byte[] pdfBytes = pdfStream.readAllBytes();
        String text;

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(document);
        }

        String expectedUnitPriceText =
                String.format("%.2f EUR", cartItem.getBaseUnitPrice().doubleValue());

        Pattern taxPattern = Pattern.compile("19\\s*%");

        assertAll(
                // Steuersatz (auch wenn Leerzeichen fehlt)
                () -> assertTrue(taxPattern.matcher(text).find(),
                        "Steuersatz (19 %) wird im Beleg nicht angezeigt."),

                // Einzelpreis
                () -> assertTrue(text.contains(expectedUnitPriceText),
                        "Einzelpreis (" + expectedUnitPriceText + ") wird im Beleg nicht angezeigt.")
        );
    }

    /**
     * Der Beleg unterscheidet zwischen Kartenzahlung und Bargeldzahlung via Flag isCashPayment.
     */
    @Test
    void receiptShowsCardPaymentText_whenCardPaymentIsUsed() throws Exception {
        Article article = new Article();
        article.setName("Kartentest");
        article.setSellingPrice(10.00);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

        CartItem cartItem = new CartItem(article, 1, 1, null);
        ReceiptService receiptService = new ReceiptService();

        ByteArrayInputStream pdfStream = receiptService.generateReceipt(List.of(cartItem), false); // false = Karte
        byte[] pdfBytes = pdfStream.readAllBytes();
        String text;

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(document);
        }

        assertAll(
                () -> assertTrue(text.contains("Kartenzahlung"),
                        "Zahlungsart 'Kartenzahlung' wird nicht ausgegeben."),
                () -> assertFalse(text.contains("Bargeldzahlung"),
                        "Für Kartenzahlung darf nicht 'Bargeldzahlung' erscheinen.")
        );
    }

    /**
     * Reihenfolge der Positionen im PDF entspricht der Position im CartItem.
     */
    @Test
    void itemOrderOnReceipt_matchesCartItemPositions() throws Exception {
        Article article1 = new Article();
        article1.setName("Artikel A");
        article1.setSellingPrice(5.00);
        article1.setTaxRatePercent(19.0);
        article1.setIsAvailable(true);

        Article article2 = new Article();
        article2.setName("Artikel B");
        article2.setSellingPrice(7.50);
        article2.setTaxRatePercent(7.0);
        article2.setIsAvailable(true);

        CartItem item1 = new CartItem(article1, 1, 1, null);
        CartItem item2 = new CartItem(article2, 2, 1, null);

        ReceiptService receiptService = new ReceiptService();

        ByteArrayInputStream pdfStream = receiptService.generateReceipt(List.of(item1, item2), true);
        byte[] pdfBytes = pdfStream.readAllBytes();
        String text;

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            text = new PDFTextStripper().getText(document);
        }

        int indexA = text.indexOf("Artikel A");
        int indexB = text.indexOf("Artikel B");

        assertTrue(indexA >= 0 && indexB >= 0, "Beide Artikel müssen im Beleg auftauchen.");
        assertTrue(indexA < indexB, "Artikelreihenfolge im Beleg entspricht nicht der Reihenfolge im Warenkorb.");
    }

    /**
     * Rabattinformation („xx % x Anzahl“) wird im Belegtext angezeigt.
     */
    @Test
    void receiptShowsDiscountInformation_whenDiscountIsApplied() throws Exception {
        Article article = new Article();
        article.setName("Rabatt-Artikel");
        article.setSellingPrice(20.00);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

        // Menge 3, davon 2 rabattiert mit 30 %
        CartItem item = new CartItem(article, 1, 3, null);
        item.setDiscountPercent(new BigDecimal("30"));
        item.setDiscountedQuantity(2);

        ReceiptService receiptService = new ReceiptService();

        ByteArrayInputStream pdfStream = receiptService.generateReceipt(List.of(item), true);
        byte[] pdfBytes = pdfStream.readAllBytes();
        String text;

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            text = new PDFTextStripper().getText(document);
        }

        // 30 % x 2 – aber tolerant bzgl. Leerzeichen
        Pattern discountPattern = Pattern.compile("\\b30\\s*%\\s*x\\s*2\\b");

        assertTrue(discountPattern.matcher(text).find(),
                "Rabattinformationen (Prozentsatz x Menge) werden auf dem Beleg nicht angezeigt.");
    }

    /**
     * getDiscountedUnitPrice() reduziert den Basispreis fachlich korrekt.
     */
    @Test
    void discountedUnitPrice_isReducedByGivenPercent() {
        Article article = new Article();
        article.setSellingPrice(100.00);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

        // 30 % Rabatt, 1 Stück
        CartItem item = new CartItem(article, 1, 30, null);
        item.setDiscountedQuantity(1);

        BigDecimal discountedUnitPrice = item.getDiscountedUnitPrice();

        BigDecimal expected = new BigDecimal("70.00");

        assertEquals(0, expected.compareTo(discountedUnitPrice),
                "Rabattierter Stückpreis (30 %) ist nicht korrekt berechnet.");
    }

    /**
     *  FOLGENDER FIX WIRD EMPFOHLEN:
     *
     *  public BigDecimal getDiscountedUnitPrice() {
     *     BigDecimal base = getBaseUnitPrice();
     *
     *     // Kein Rabatt gepflegt -> Basispreis zurückgeben
     *     if (!hasDiscount()) {
     *         return base.setScale(2, RoundingMode.HALF_UP);
     *     }
     *
     *     // discountPercent ist z. B. 30 für 30 %
     *     BigDecimal percent = discountPercent != null ? discountPercent : BigDecimal.ZERO;
     *
     *     // Faktor = 1 - (percent / 100), also z.B. 1 - 0,30 = 0,70
     *     BigDecimal factor = BigDecimal.ONE.subtract(
     *             percent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
     *     );
     *
     *     return base
     *             .multiply(factor)
     *             .setScale(2, RoundingMode.HALF_UP);
     * }
     *
     * UND:
     *
     * public boolean hasDiscount() {
     *     return discountPercent != null
     *             && discountedQuantity != null
     *             && discountPercent.compareTo(BigDecimal.ZERO) > 0
     *             && discountedQuantity > 0;
     * }
     */

    /**
     * getTotalPriceWithDiscount() rabattiert nur die konfigurierte Stückzahl.
     */
    @Test
    void totalPriceWithDiscount_appliesPartialDiscountOnlyToDiscountedQuantity() {
        Article article = new Article();
        article.setSellingPrice(10.00);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

        // 5 Stück, 30 % Rabatt, davon 2 rabattiert
        CartItem item = new CartItem(article, 5, 30, null);
        item.setDiscountedQuantity(2);

        // Erwartung: 2 * 7,00 + 3 * 10,00 = 44,00
        BigDecimal expected = new BigDecimal("44.00");

        BigDecimal total = item.getTotalPriceWithDiscount();

        assertEquals(0, expected.compareTo(total),
                "Teilrabatt (nur eine Teilmenge rabattiert) wird nicht korrekt berechnet.");
    }

    /**
     *  FOLGENDER FIX WIRD EMPFOHLEN:
     *
     *  public BigDecimal getTotalPriceWithDiscount() {
     *     BigDecimal unitBasePrice = getBaseUnitPrice(); // z. B. 10.00
     *
     *     int qty = this.getQuantity(); // oder this.quantity, je nach Implementierung
     *     int discQty = this.getDiscountedQuantity() != null ? this.getDiscountedQuantity() : 0;
     *
     *     // Kein Rabatt konfiguriert -> ganz normaler Gesamtpreis
     *     if (!hasDiscount() || discQty <= 0) {
     *         return unitBasePrice
     *                 .multiply(BigDecimal.valueOf(qty))
     *                 .setScale(2, RoundingMode.HALF_UP);
     *     }
     *
     *     // Rabattierte Menge darf nicht größer als Gesamtmenge sein
     *     if (discQty > qty) {
     *         discQty = qty;
     *     }
     *
     *     BigDecimal discountedUnitPrice = getDiscountedUnitPrice(); // z. B. 7.00
     *
     *     BigDecimal discountedPart = discountedUnitPrice
     *             .multiply(BigDecimal.valueOf(discQty));          // 2 * 7.00
     *
     *     BigDecimal fullPricePart = unitBasePrice
     *             .multiply(BigDecimal.valueOf(qty - discQty));    // 3 * 10.00
     *
     *     return discountedPart
     *             .add(fullPricePart)                              // = 44.00
     *             .setScale(2, RoundingMode.HALF_UP);
     * }
     */

    /**
     * hasDiscount() erkennt korrekt, ob ein Rabatt aktiv ist.
     */
    @Test
    void hasDiscount_isFalseWhenPercentOrQuantityInvalid() {
        Article article = new Article();
        article.setSellingPrice(10.00);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

        // Standard-Artikel ohne Rabatt
        CartItem item = new CartItem(article, 1, 0, null);

        // 1) kein Prozent + keine Menge -> kein Rabatt
        assertFalse(item.hasDiscount(), "Ohne Prozent und Menge darf kein Rabatt aktiv sein.");

        // 2) Menge vorhanden, aber Prozent = 0 -> kein Rabatt
        item.setDiscountPercent(BigDecimal.ZERO);
        item.setDiscountedQuantity(1);
        assertFalse(item.hasDiscount(), "Rabatt 0 % darf nicht als aktiver Rabatt gewertet werden.");

        // 3) Prozent vorhanden, aber Menge = 0 -> kein Rabatt
        item.setDiscountPercent(new BigDecimal("10"));
        item.setDiscountedQuantity(0);
        assertFalse(item.hasDiscount(), "Rabatt mit Menge 0 darf nicht als aktiver Rabatt gewertet werden.");
    }

    /**
     * CartItem verwendet den Overridden-Preis als Basispreis, wenn vorhanden.
     */
    @Test
    void baseUnitPrice_prefersOverriddenPriceOverArticlePrice() {
        Article article = new Article();
        article.setSellingPrice(15.00);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

        // Kein Rabatt, Menge 1. Override-Preis wird explizit gesetzt
        CartItem item = new CartItem(article, 1, 0, null);
        item.setOverriddenPrice(new BigDecimal("12.34"));

        BigDecimal basePrice = item.getBaseUnitPrice();

        BigDecimal expected = new BigDecimal("12.34");

        assertEquals(0, expected.compareTo(basePrice),
                "Overridden Price muss Vorrang vor dem im Artikel hinterlegten Verkaufspreis haben.");
    }

    /**
     * Konstante MIN_PRICE der CashierView ist korrekt gesetzt.
     */
    @Test
    void minPriceConstant_isAtLeastOneCent() throws Exception {
        var field = CashierView.class.getDeclaredField("MIN_PRICE");
        field.setAccessible(true);
        BigDecimal minPrice = (BigDecimal) field.get(null);

        BigDecimal expected = new BigDecimal("0.01");

        assertEquals(0, expected.compareTo(minPrice),
                "MIN_PRICE muss 0,01 € sein, um negative/ungültige Preise zu verhindern.");
    }

    /**
     * CartItemsManager verwaltet pro Benutzer einen eigenen Warenkorb.
     */
    @Test
    void cartIsStoredPerUser_andSeparatedBetweenUsers() {
        CartItemsManager manager = new CartItemsManager();

        // user1: Artikel hinzufügen
        setSecurityUser("user1");
        List<CartItem> cartUser1 = manager.getCart();
        cartUser1.add(dummyCartItem("User1-Artikel"));

        // user2: eigener, zunächst leerer Warenkorb
        setSecurityUser("user2");
        List<CartItem> cartUser2 = manager.getCart();

        // Basisprüfungen
        assertAll(
                () -> assertNotSame(cartUser1, cartUser2,
                        "Jeder Benutzer muss eine eigene Warenkorb-Liste erhalten."),
                () -> assertEquals(1, cartUser1.size(),
                        "Warenkorb von user1 sollte 1 Artikel enthalten."),
                () -> assertEquals(0, cartUser2.size(),
                        "Warenkorb von user2 muss unabhängig von user1 sein.")
        );

        // user2 fügt jetzt auch einen Artikel hinzu
        cartUser2.add(dummyCartItem("User2-Artikel"));

        // Sicherstellen, dass user1-Warenkorb nicht betroffen ist
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
     * Daten bleiben über mehrere Zugriffe des gleichen Users in der Map erhalten.
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
     * clearCart() leert nur den Warenkorb des aktuellen Benutzers.
     */
    @Test
    void clearCart_emptiesOnlyCurrentUsersCart() {
        CartItemsManager manager = new CartItemsManager();

        // User A bekommt Artikel
        setSecurityUser("closer");
        List<CartItem> cartA = manager.getCart();
        cartA.add(dummyCartItem("Artikel A"));

        // User B bekommt anderen Artikel
        setSecurityUser("other");
        List<CartItem> cartB = manager.getCart();
        cartB.add(dummyCartItem("Artikel B"));

        // Nun User A’s Warenkorb leeren
        setSecurityUser("closer");
        manager.clearCart();

        // Prüfen: User A leer, User B unverändert
        setSecurityUser("closer");
        assertEquals(0, manager.getCart().size(),
                "Nach clearCart() muss der Warenkorb des aktuellen Benutzers leer sein.");

        setSecurityUser("other");
        assertEquals(1, manager.getCart().size(),
                "clearCart() darf nicht den Warenkorb anderer Benutzer löschen.");
    }

    /**
     * ArticleService liefert bei gültiger Artikelnummer ein Optional mit Artikel und delegiert an das Repository.
     */
    @Test
    void findByArticleNumber_returnsArticleForValidNumber() {
        ArticleRepository repo = Mockito.mock(ArticleRepository.class);
        ArticleService service = new ArticleService(repo);

        Article article = new Article();
        article.setArticleNumber("A-123");
        article.setName("Scanner-Artikel");
        article.setSellingPrice(1.23);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

        when(repo.findByArticleNumber("A-123")).thenReturn(Optional.of(article));

        Optional<Article> result = service.findByArticleNumber("A-123");

        assertAll(
                () -> assertTrue(result.isPresent(), "Für eine gültige Artikelnummer muss ein Artikel gefunden werden."),
                () -> assertEquals("Scanner-Artikel", result.get().getName(),
                        "Artikelname des gefundenen Artikels ist unerwartet."),
                () -> assertEquals(1.23, result.get().getSellingPrice(), 0.0001,
                        "Verkaufspreis des gefundenen Artikels ist unerwartet."),
                () -> assertSame(article, result.get(),
                        "ArticleService sollte genau den vom Repository gelieferten Artikel zurückgeben.")
        );

        verify(repo, times(1)).findByArticleNumber("A-123");
        verifyNoMoreInteractions(repo);
    }

    /**
     * ArticleService gibt Optional.empty() für unbekannte Artikelnummer zurück.
     */
    @Test
    void findByArticleNumber_returnsEmptyForUnknownNumber() {
        ArticleRepository repo = Mockito.mock(ArticleRepository.class);
        ArticleService service = new ArticleService(repo);

        when(repo.findByArticleNumber("A-999")).thenReturn(Optional.empty());

        Optional<Article> result = service.findByArticleNumber("A-999");

        assertTrue(result.isEmpty(),
                "Für eine unbekannte Artikelnummer darf kein Artikel zurückgegeben werden.");

        verify(repo, times(1)).findByArticleNumber("A-999");
        verifyNoMoreInteractions(repo);
    }

    /**
     * RoleView ist über @RolesAllowed ausschließlich für ROLE_ADMIN freigeschaltet.
     */
    @Test
    void roleView_isRestrictedToAdminsViaRolesAllowedAnnotation() {
        RolesAllowed rolesAllowed = RoleView.class.getAnnotation(RolesAllowed.class);

        assertNotNull(rolesAllowed, "RoleView muss mit @RolesAllowed abgesichert sein.");

        List<String> allowed = List.of(rolesAllowed.value());

        assertAll(
                () -> assertTrue(allowed.contains(AccountRoleEnum.ROLE_ADMIN.toString()),
                        "RoleView muss für ROLE_ADMIN zugänglich sein."),
                () -> assertEquals(1, allowed.size(),
                        "RoleView darf ausschließlich für ROLE_ADMIN freigeschaltet sein.")
        );
    }

    /**
     * Entity Account besitzt genau ein ManyToOne-Feld auf AccountRole.
     */
    @Test
    void accountHasExactlyOneRole_viaManyToOneMapping() throws Exception {
        // 1) Konkretes Feld prüfen
        Field field = Account.class.getDeclaredField("accountRole");

        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);

        assertNotNull(manyToOne, "Feld 'accountRole' muss mit @ManyToOne annotiert sein.");
        assertNotNull(joinColumn, "Feld 'accountRole' muss eine @JoinColumn besitzen.");
        assertFalse(joinColumn.nullable(),
                "Account muss genau eine (nicht nullbare) Rolle besitzen.");

        // 2) Sicherstellen, dass es nicht weitere Mappings auf AccountRole gibt
        long roleMappings = Arrays.stream(Account.class.getDeclaredFields())
                .filter(f -> f.getType().equals(AccountRole.class))
                .filter(f -> f.getAnnotation(ManyToOne.class) != null)
                .count();

        assertEquals(1, roleMappings,
                "Account darf genau eine ManyToOne-Beziehung auf AccountRole besitzen.");
    }

    /**
     * AccountRoleEnum liefert korrekte Authorities im Spring-Security-Sinne (ROLE_…).
     */
    @Test
    void accountRoleEnum_getAuthorityHasRolePrefix() {
        AccountRoleEnum adminRole = AccountRoleEnum.ADMIN;

        String authority = adminRole.getAuthority();

        assertEquals("ROLE_" + adminRole.name(), authority,
                "ADMIN-Rolle muss Authority 'ROLE_ADMIN' (ROLE_ + Enum-Name) besitzen.");
    }

    /**
     * Alle Account-Rollen müssen Authorities mit Prefix ROLE_ besitzen.
     */
    @Test
    void allAccountRoles_haveProperRolePrefix() {
        for (AccountRoleEnum role : AccountRoleEnum.values()) {
            assertTrue(role.getAuthority().startsWith("ROLE_"),
                    role + " muss mit ROLE_ anfangen.");
        }
    }

    /**
     * AccountRoleService delegiert findByRole() an das Repository – damit Änderungen an Rollen live
     *  aus der Datenbank gelesen werden können.
     */
    @Test
    void accountRoleService_findByRoleDelegatesToRepository() {
        AccountRoleRepository repo = Mockito.mock(AccountRoleRepository.class);
        AccountRoleService service = new AccountRoleService(repo);

        AccountRole role = new AccountRole();
        role.setRole(AccountRoleEnum.ADMIN);

        when(repo.findByRole(AccountRoleEnum.ADMIN)).thenReturn(Optional.of(role));

        Optional<AccountRole> result = service.findByRole(AccountRoleEnum.ADMIN);

        assertAll(
                () -> assertTrue(result.isPresent(), "AccountRoleService muss Rollen aus dem Repository liefern."),
                () -> assertEquals(AccountRoleEnum.ADMIN, result.get().getRole(),
                        "AccountRoleService muss die korrekte Rolle zurückgeben."),
                () -> assertSame(role, result.get(),
                        "Service muss exakt das vom Repository gelieferte Objekt zurückgeben.")
        );

        verify(repo, times(1)).findByRole(AccountRoleEnum.ADMIN);
        verifyNoMoreInteractions(repo);
    }

    /**
     * Hilfsmethode: Setzt einen authentifizierten Benutzer im SecurityContext,
     * damit CartItemsManager.currentUsername() nicht auf "guest" zurückfällt.
     */
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

    /**
     * Hilfsmethode: Erzeugt einen einfachen CartItem mit minimal benötigten Daten.
     */
    private CartItem dummyCartItem(String name) {
        Article article = new Article();
        article.setName(name);
        article.setSellingPrice(1.00);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);
        return new CartItem(article, 1, 1, null);
    }
}