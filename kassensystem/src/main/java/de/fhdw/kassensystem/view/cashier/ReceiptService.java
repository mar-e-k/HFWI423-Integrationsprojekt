package de.fhdw.kassensystem.view.cashier;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReceiptService {

    public ReceiptService() {

    }

    public ByteArrayInputStream generateReceipt(List<CartItem> cartItems, boolean isCashPayment, String cashierName, String cashierPersonnelNumber, String depositRedemptionCode, boolean isDepositOnlyReceipt, Long storeId, Long registerId) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            float y = 750;
            float margin = 50;

            contentStream.beginText();
            contentStream.setFont(boldFont, 18);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText(isDepositOnlyReceipt ? "Pfandbon" : "Kaufbeleg"); // Titel anpassen
            contentStream.endText();
            y -= 30;

            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Datum: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
            contentStream.endText();
            y -= 20;

            contentStream.moveTo(margin, y);
            contentStream.lineTo(550, y);
            contentStream.stroke();
            y -= 20;

            contentStream.beginText();
            contentStream.setFont(boldFont, 12);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Filiale: %d".formatted(storeId));
            contentStream.endText();

            y -= 15;

            contentStream.beginText();
            contentStream.setFont(boldFont, 12);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Kasse: %d".formatted(registerId));
            contentStream.endText();

            y -= 15;

            contentStream.beginText();
            contentStream.setFont(boldFont, 12);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Kassierer: %s %s".formatted(cashierPersonnelNumber, cashierName));
            contentStream.endText();

            y -= 15;

            contentStream.beginText();
            contentStream.setFont(boldFont, 12);
            contentStream.newLineAtOffset(margin, y);
            if (!isDepositOnlyReceipt) {
                contentStream.showText("Belegart: BELEG");
            } else {
                contentStream.showText("Belegart: PFAND");
            }
            contentStream.endText();

            y -= 15;

            contentStream.moveTo(margin, y);
            contentStream.lineTo(550, y);
            contentStream.stroke();
            y -= 20;


            contentStream.beginText();
            contentStream.setFont(boldFont, 12);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Pos.");
            contentStream.newLineAtOffset(50, 0);
            contentStream.showText("Artikel");
            contentStream.newLineAtOffset(150, 0);
            contentStream.showText("Menge");
            contentStream.newLineAtOffset(70, 0);
            contentStream.showText("Steuersatz");
            contentStream.newLineAtOffset(100, 0);
            contentStream.showText("Preis");
            contentStream.newLineAtOffset(80, 0);
            contentStream.showText("Rabatt");
            contentStream.endText();
            y -= 20;

            BigDecimal total = BigDecimal.ZERO;

            for (CartItem item : cartItems) {
                BigDecimal itemTotal = item.getTotalPriceWithDiscount();
                total = total.add(itemTotal);

                contentStream.beginText();
                contentStream.setFont(font, 10);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText(String.valueOf(item.getPosition()));
                contentStream.newLineAtOffset(50, 0);
                if (item.getArticle().getName().length() > 25) {
                    contentStream.showText(item.getArticle().getName().substring(0, 22) + "...");
                } else {
                    contentStream.showText(item.getArticle().getName());
                }
                contentStream.newLineAtOffset(150, 0);
                contentStream.showText(String.valueOf(item.getQuantity()));
                contentStream.newLineAtOffset(70, 0);
                contentStream.showText(String.format("%d %%", item.getArticle().getTaxRatePercent().intValue()));
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText(String.format("%.2f EUR", item.getBaseUnitPrice().doubleValue()));
                contentStream.newLineAtOffset(80, 0);
                contentStream.showText(String.format("%d %% x %d", item.getDiscountPercent().intValue(), item.getDiscountedQuantity()));
                contentStream.endText();
                y -= 20;
            }

            contentStream.moveTo(margin, y);
            contentStream.lineTo(550, y);
            contentStream.stroke();
            y -= 30;

            contentStream.beginText();
            contentStream.setFont(boldFont, 14);
            contentStream.newLineAtOffset(350, y);
            contentStream.showText("Gesamtbetrag: " + String.format("%.2f EUR", total));
            contentStream.endText();

            // Zahlungsart nur anzeigen, wenn es kein reiner Pfandbon ist
            if (!isDepositOnlyReceipt) {
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(50, y);
                String paymentText = isCashPayment ? "Bargeldzahlung" : "Kartenzahlung";
                contentStream.showText(paymentText);
                contentStream.endText();
                y -= 30;
            }

            // Pfandbon-Code hinzufügen, wenn vorhanden
            if (depositRedemptionCode != null && !depositRedemptionCode.isEmpty()) {
                contentStream.beginText();
                contentStream.setFont(boldFont, 14);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Pfandbon Code: " + depositRedemptionCode);
                contentStream.endText();
                y -= 20;
            }

            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Vielen Dank für Ihren Einkauf!");
            contentStream.endText();

            contentStream.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);

            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
