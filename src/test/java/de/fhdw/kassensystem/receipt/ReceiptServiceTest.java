package de.fhdw.kassensystem.receipt;

import de.fhdw.kassensystem.persistence.entity.imported.Article;
import de.fhdw.kassensystem.persistence.service.ReceiptService;
import de.fhdw.kassensystem.view.cashier.CartItem;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptServiceTest {

    /**
     * SCRUM-29 / SCRUM-53 – Zahlung mit Bargeld / Belegerzeugung:
     * Prüft, dass bei einer Barzahlung ein Beleg mit allen Pflichtfeldern (Datum, Artikel,
     * Einzelpreise, Rabatt, Steuersatz, Zahlungsart, Gesamtbetrag) erzeugt wird.
     */
    @Test
    void receiptContainsMandatoryFields_forCashPayment() throws Exception {
        Article article = new Article();
        article.setName("Test-Artikel");
        article.setSellingPrice(9.99);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

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
        String expectedUnitPriceText = String.format("%.2f EUR", cartItem.getBaseUnitPrice().doubleValue());
        String expectedTaxRateText = String.format("%d %%", article.getTaxRatePercent().intValue());
        String expectedDiscountText = String.format("%d %% x %d",
                cartItem.getDiscountPercent().intValue(), cartItem.getDiscountedQuantity());

        assertAll(
                () -> assertTrue(text.contains("Kaufbeleg"), "Beleg-Titel (Kaufbeleg) fehlt."),
                () -> assertTrue(text.contains("Datum:"), "Datum/Uhrzeit wird nicht ausgegeben."),
                () -> assertTrue(text.contains("Test-Artikel"), "Artikelname fehlt im Beleg."),
                () -> assertTrue(text.contains(expectedUnitPriceText), "Einzelpreis wird nicht korrekt angezeigt."),
                () -> assertTrue(text.contains(expectedDiscountText), "Rabatt wird nicht korrekt angezeigt."),
                () -> assertTrue(text.contains(expectedTaxRateText), "Steuersatz wird nicht korrekt angezeigt."),
                () -> assertTrue(text.contains(expectedTotalText), "Gesamtbetrag wird nicht korrekt angezeigt."),
                () -> assertTrue(text.contains("Bargeldzahlung"), "Zahlungsart 'Bargeldzahlung' wird nicht angezeigt.")
        );
    }

    /**
     * SCRUM-53 – Belegerzeugung:
     * Stellt sicher, dass Datum und Uhrzeit auf dem Beleg im geforderten Format
     * „dd.MM.yyyy HH:mm:ss“ ausgegeben werden.
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
     * SCRUM-53 / SCRUM-57 – Belegerzeugung / Kassenbon-Übersicht:
     * Prüft, dass Steuersatz und Einzelpreise auf dem Beleg korrekt dargestellt werden,
     * damit Preise und Steueranteile für den Kunden nachvollziehbar sind.
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

        String expectedUnitPriceText = String.format("%.2f EUR", cartItem.getBaseUnitPrice().doubleValue());
        Pattern taxPattern = Pattern.compile("19\\s*%");

        assertAll(
                () -> assertTrue(taxPattern.matcher(text).find(),
                        "Steuersatz (19 %) wird im Beleg nicht angezeigt."),
                () -> assertTrue(text.contains(expectedUnitPriceText),
                        "Einzelpreis (" + expectedUnitPriceText + ") wird im Beleg nicht angezeigt.")
        );
    }

    /**
     * SCRUM-35 – Zahlung mit Karte:
     * Stellt sicher, dass bei Kartenzahlung die Zahlungsart korrekt als „Kartenzahlung“
     * auf dem Beleg erscheint und keine Barzahlung ausgewiesen wird.
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
     * SCRUM-57 – Kassenbon-Übersicht:
     * Prüft, dass die Reihenfolge der Artikel auf dem Beleg der Reihenfolge im Warenkorb entspricht,
     * damit der Kassenbon für den Kassierer und Kunden nachvollziehbar bleibt.
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
     * SCRUM-57 / SCRUM-58 – Kassenbon-Übersicht / Rabattfunktion:
     * Verifiziert, dass Rabattinformationen im Format „xx % x Anzahl“ auf dem Beleg
     * erscheinen und damit Rabatte auf dem Bon nachvollziehbar dargestellt werden.
     */
    @Test
    void receiptShowsDiscountInformation_whenDiscountIsApplied() throws Exception {
        Article article = new Article();
        article.setName("Rabatt-Artikel");
        article.setSellingPrice(20.00);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

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

        Pattern discountPattern = Pattern.compile("\\b30\\s*%\\s*x\\s*2\\b");
        assertTrue(discountPattern.matcher(text).find(),
                "Rabattinformationen (Prozentsatz x Menge) werden auf dem Beleg nicht angezeigt.");
    }
}