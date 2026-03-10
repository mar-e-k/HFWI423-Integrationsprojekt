package de.fhdw.vendix.commons.core.printer.api;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.core.printer.renderer.ReceiptRenderer;
import de.fhdw.vendix.commons.core.printer.renderer.receipt.*;

public final class ReceiptPrinter {

    private ReceiptPrinter() {}

    public static byte[] print(ReceiptDTO receipt, ReceiptType type) {
        if (receipt == null) {
            throw new IllegalArgumentException("Parameter 'receipt' cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Parameter 'type' cannot be null");
        }

        ReceiptRenderer receiptRenderer = switch (type) {
            case STANDARD -> new StandardReceiptRenderer();
            case DAILY -> new DailyReceiptRenderer();
            case VOUCHER -> new VoucherReceiptRenderer();
        };

        return receiptRenderer.render(receipt);
    }

//    public ByteArrayInputStream generateReceipt(Receipt receipt, boolean isCashPayment) {
//        try {
//            List<ReceiptLinkArticle> articles = receiptLinkArticleRepository.findByReceipt(receipt);
//
//            PDDocument document = new PDDocument();
//            PDPage page = new PDPage();
//            document.addPage(page);
//
//            PDPageContentStream contentStream = new PDPageContentStream(document, page);
//
//            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
//            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
//
//            float y = 750;
//            float margin = 50;
//
//            contentStream.beginText();
//            contentStream.setFont(boldFont, 18);
//            contentStream.newLineAtOffset(margin, y);
//            contentStream.showText("Kaufbeleg");
//            contentStream.endText();
//            y -= 30;
//
//            contentStream.beginText();
//            contentStream.setFont(font, 12);
//            contentStream.newLineAtOffset(margin, y);
//            contentStream.showText("Datum: " + receipt.getCreatedAt().atZone(ZoneId.systemDefault()).format(fmt));
//            contentStream.endText();
//            y -= 20;
//
//            contentStream.moveTo(margin, y);
//            contentStream.lineTo(550, y);
//            contentStream.stroke();
//            y -= 20;
//
//            contentStream.beginText();
//            contentStream.setFont(boldFont, 12);
//            contentStream.newLineAtOffset(margin, y);
//            contentStream.showText("Filiale: %d %s-%s-%s".formatted(receipt.getStore().getId(), receipt.getStore().getCity(), receipt.getStore().getStreet(), receipt.getStore().getStreetNumber()));
//            contentStream.endText();
//
//            y -= 15;
//
//            contentStream.beginText();
//            contentStream.setFont(boldFont, 12);
//            contentStream.newLineAtOffset(margin, y);
//            contentStream.showText("Kasse: %d".formatted(receipt.getRegister().getId()));
//            contentStream.endText();
//
//            y -= 15;
//
//            contentStream.beginText();
//            contentStream.setFont(boldFont, 12);
//            contentStream.newLineAtOffset(margin, y);
//            contentStream.showText("Kassierer: %s %d".formatted(receipt.getAccount().getUsername(), receipt.getAccount().getId()));
//            contentStream.endText();
//
//            y -= 15;
//
//            contentStream.beginText();
//            contentStream.setFont(boldFont, 12);
//            contentStream.newLineAtOffset(margin, y);
//            if (receipt.getDepositRedemptionCode() == null) {
//                contentStream.showText("Belegart: BELEG");
//            } else {
//                contentStream.showText("Belegart: PFAND");
//            }
//            contentStream.endText();
//
//            y -= 15;
//
//            contentStream.moveTo(margin, y);
//            contentStream.lineTo(550, y);
//            contentStream.stroke();
//            y -= 20;
//
//            contentStream.beginText();
//            contentStream.setFont(boldFont, 12);
//            contentStream.newLineAtOffset(margin, y);
//            contentStream.showText("Pos.");
//            contentStream.newLineAtOffset(50, 0);
//            contentStream.showText("Artikel");
//            contentStream.newLineAtOffset(150, 0);
//            contentStream.showText("Menge");
//            contentStream.newLineAtOffset(70, 0);
//            contentStream.showText("Steuersatz");
//            contentStream.newLineAtOffset(100, 0);
//            contentStream.showText("Preis");
//            contentStream.newLineAtOffset(80, 0);
//            contentStream.showText("Rabatt");
//            contentStream.endText();
//            y -= 20;
//
//            for (int i = 0; i < articles.size(); i++) {
//                ReceiptLinkArticle article = articles.get(i);
//                contentStream.beginText();
//                contentStream.setFont(font, 10);
//                contentStream.newLineAtOffset(margin, y);
//                contentStream.showText(String.valueOf(i + 1));
//                contentStream.newLineAtOffset(50, 0);
//                if (article.getArticle().getName().length() > 25) {
//                    contentStream.showText(article.getArticle().getName().substring(0, 22) + "...");
//                } else {
//                    contentStream.showText(article.getArticle().getName());
//                }
//                contentStream.newLineAtOffset(150, 0);
//                contentStream.showText(String.valueOf(article.getAmount()));
//                contentStream.newLineAtOffset(70, 0);
//                contentStream.showText(String.format("%d %%", article.getArticle().getTaxRatePercent().intValue()));
//                contentStream.newLineAtOffset(100, 0);
//                if (article.getOverridePrice() != null && article.getOverridePrice().compareTo(BigDecimal.ZERO) > 0) {
//                    contentStream.showText(String.format("%.2f EUR", article.getOverridePrice().doubleValue()));
//                    contentStream.newLineAtOffset(80, 0);
//                } else {
//                    contentStream.showText(String.format("%.2f EUR", article.getPrice().doubleValue()));
//                    contentStream.newLineAtOffset(80, 0);
//                }
//                if (article.getDiscountedByPercent() != null && article.getDiscountedByPercent().compareTo(BigDecimal.ZERO) > 0) {
//                    contentStream.showText(String.format("%d %% x %d", article.getDiscountedByPercent().intValue(), article.getAmount()));
//                } else {
//                    contentStream.showText("Kein Rabatt");
//                }
//                contentStream.endText();
//                y -= 20;
//            }
//
//            contentStream.moveTo(margin, y);
//            contentStream.lineTo(550, y);
//            contentStream.stroke();
//            y -= 30;
//
//            contentStream.beginText();
//            contentStream.setFont(boldFont, 14);
//            contentStream.newLineAtOffset(350, y);
//            contentStream.showText("Gesamtbetrag: " + String.format("%.2f EUR", receipt.getTotalAmount()));
//            contentStream.endText();
//
//            contentStream.beginText();
//            contentStream.setFont(font, 12);
//            contentStream.newLineAtOffset(50, y);
//            String paymentText = isCashPayment ? "Bargeldzahlung" : "Kartenzahlung";
//            contentStream.showText(paymentText);
//            contentStream.endText();
//            y -= 30;
//
//            contentStream.beginText();
//            contentStream.setFont(font, 12);
//            contentStream.newLineAtOffset(margin, y);
//            contentStream.showText("Vielen Dank für Ihren Einkauf!");
//            contentStream.endText();
//
//            contentStream.close();
//
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            document.save(out);
//
//            return new ByteArrayInputStream(out.toByteArray());
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    public ByteArrayInputStream generateDailyReceipt(List<Receipt> receipts) {
//        try {
//            Map<Register, Map<Account, List<Receipt>>> grouped =
//                    receipts.stream().collect(
//                            Collectors.groupingBy(
//                                    Receipt::getRegister,
//                                    Collectors.groupingBy(Receipt::getAccount)
//                            )
//                    );
//
//            Map<Register, Map<Account, BigDecimal>> totalsPerAccount =
//                    grouped.entrySet().stream().collect(Collectors.toMap(
//                            Map.Entry::getKey,
//                            e -> e.getValue().entrySet().stream().collect(Collectors.toMap(
//                                    Map.Entry::getKey,
//                                    acc -> acc.getValue().stream()
//                                            .map(Receipt::getTotalAmount)
//                                            .reduce(BigDecimal.ZERO, BigDecimal::add)
//                            ))
//                    ));
//
//            Map<Register, BigDecimal> totalsPerRegister =
//                    grouped.entrySet().stream().collect(Collectors.toMap(
//                            Map.Entry::getKey,
//                            e -> e.getValue().values().stream()
//                                    .flatMap(List::stream)
//                                    .map(Receipt::getTotalAmount)
//                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
//                    ));
//
//            Map<Account, Map<Register, BigDecimal>> totalsPerCashier = new HashMap<>();
//
//            for (Map.Entry<Register, Map<Account, BigDecimal>> regEntry : totalsPerAccount.entrySet()) {
//                Register register = regEntry.getKey();
//                Map<Account, BigDecimal> accTotals = regEntry.getValue();
//
//                for (Map.Entry<Account, BigDecimal> accEntry : accTotals.entrySet()) {
//                    Account cashier = accEntry.getKey();
//                    BigDecimal total = accEntry.getValue();
//
//                    totalsPerCashier
//                            .computeIfAbsent(cashier, a -> new HashMap<>())
//                            .put(register, total);
//                }
//            }
//
//            PDDocument document = new PDDocument();
//            PDPage page = new PDPage();
//            document.addPage(page);
//
//            PDPageContentStream contentStream = new PDPageContentStream(document, page);
//
//            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
//            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
//
//            float y = 750;
//            float margin = 50;
//
//            contentStream.beginText();
//            contentStream.setFont(boldFont, 18);
//            contentStream.newLineAtOffset(margin, y);
//            contentStream.showText("Tagesabschluss");
//            contentStream.endText();
//            y -= 25;
//
//            contentStream.beginText();
//            contentStream.setFont(boldFont, 14);
//            contentStream.newLineAtOffset(margin, y);
//            contentStream.showText("Filiale: " + receipts.getFirst().getStore().getId());
//            contentStream.endText();
//            y -= 30;
//
//            contentStream.beginText();
//            contentStream.setFont(font, 12);
//            contentStream.newLineAtOffset(margin, y);
//            contentStream.showText("Datum: " + LocalDateTime.now().format(fmt));
//            contentStream.endText();
//            y -= 20;
//
//            contentStream.moveTo(margin, y);
//            contentStream.lineTo(550, y);
//            contentStream.stroke();
//            y -= 30;
//
//            contentStream.beginText();
//            contentStream.setFont(boldFont, 14);
//            contentStream.newLineAtOffset(margin, y);
//            contentStream.showText("Beleganzahl: %d".formatted(receipts.size()));
//            contentStream.endText();
//            y -= 20;
//
//            contentStream.beginText();
//            contentStream.setFont(boldFont, 14);
//            contentStream.newLineAtOffset(margin, y);
//            contentStream.showText("Kontoanzahl: %d".formatted(totalsPerAccount.values().stream()
//                    .map(Map::keySet)
//                    .flatMap(Set::stream)
//                    .collect(Collectors.toSet())
//                    .size()));
//            contentStream.endText();
//            y -= 20;
//
//            contentStream.beginText();
//            contentStream.setFont(boldFont, 14);
//            contentStream.newLineAtOffset(margin, y);
//            contentStream.showText("Kassenanzahl: %d".formatted(totalsPerRegister.size()));
//            contentStream.endText();
//            y -= 20;
//
//            contentStream.moveTo(margin, y);
//            contentStream.lineTo(550, y);
//            contentStream.stroke();
//            y -= 30;
//
//            for (Register register : totalsPerRegister.keySet()) {
//                BigDecimal registerTotal = totalsPerRegister.get(register);
//
//                contentStream.beginText();
//                contentStream.setFont(boldFont, 14);
//                contentStream.newLineAtOffset(margin, y);
//                contentStream.showText("Kasse " + register.getId() + ": " + registerTotal + " EUR");
//                contentStream.endText();
//                y -= 20;
//
//                Map<Account, BigDecimal> accountTotals = totalsPerAccount.get(register);
//
//                for (Map.Entry<Account, BigDecimal> accEntry : accountTotals.entrySet()) {
//                    contentStream.beginText();
//                    contentStream.setFont(font, 12);
//                    contentStream.newLineAtOffset(margin + 30, y);
//                    contentStream.showText("Kassierer " + accEntry.getKey().getUsername() + ": " + accEntry.getValue() + " EUR");
//                    contentStream.endText();
//                    y -= 18;
//                }
//
//                y -= 10;
//            }
//
//            contentStream.moveTo(margin, y);
//            contentStream.lineTo(550, y);
//            contentStream.stroke();
//            y -= 30;
//
//            for (Map.Entry<Account, Map<Register, BigDecimal>> cashierEntry : totalsPerCashier.entrySet()) {
//
//                Account cashier = cashierEntry.getKey();
//                Map<Register, BigDecimal> perRegister = cashierEntry.getValue();
//
//                contentStream.beginText();
//                contentStream.setFont(boldFont, 14);
//                contentStream.newLineAtOffset(margin, y);
//                contentStream.showText(
//                        "Kassierer " + cashier.getUsername() + ": " +
//                                String.format("%.2f EUR",
//                                        perRegister.values().stream()
//                                                .reduce(BigDecimal.ZERO, BigDecimal::add))
//                );
//                contentStream.endText();
//                y -= 20;
//
//                for (Map.Entry<Register, BigDecimal> regEntry : perRegister.entrySet()) {
//
//                    contentStream.beginText();
//                    contentStream.setFont(font, 12);
//                    contentStream.newLineAtOffset(margin + 30, y);
//                    contentStream.showText(
//                            "Kasse " + regEntry.getKey().getId() + ": " +
//                                    String.format("%.2f EUR", regEntry.getValue())
//                    );
//                    contentStream.endText();
//                    y -= 18;
//                }
//
//                y -= 10;
//            }
//
//
//            contentStream.moveTo(margin, y);
//            contentStream.lineTo(550, y);
//            contentStream.stroke();
//            y -= 30;
//
//            contentStream.beginText();
//            contentStream.setFont(boldFont, 12);
//            contentStream.newLineAtOffset(margin, y);
//            contentStream.showText("Beleg-ID");
//            contentStream.newLineAtOffset(70, 0);
//            contentStream.showText("Beleg-Art");
//            contentStream.newLineAtOffset(90, 0);
//            contentStream.showText("Kasse");
//            contentStream.newLineAtOffset(70, 0);
//            contentStream.showText("Kassierer");
//            contentStream.newLineAtOffset(90, 0);
//            contentStream.showText("Preis");
//            contentStream.newLineAtOffset(60, 0);
//            contentStream.showText("Datum");
//            contentStream.endText();
//            y -= 20;
//
//            for (Receipt receipt : receipts) {
//                contentStream.beginText();
//                contentStream.setFont(font, 11);
//                contentStream.newLineAtOffset(margin, y);
//                contentStream.showText("%d".formatted(receipt.getId()));
//                contentStream.newLineAtOffset(70, 0);
//                if (receipt.isDepositOnly()) {
//                    contentStream.showText("PFAND");
//                } else {
//                    contentStream.showText("BELEG");
//                }
//                contentStream.newLineAtOffset(90, 0);
//                contentStream.showText("%d".formatted(receipt.getRegister().getId()));
//                contentStream.newLineAtOffset(70, 0);
//                contentStream.showText("%s".formatted(receipt.getAccount().getUsername()));
//                contentStream.newLineAtOffset(90, 0);
//                contentStream.showText("%s".formatted(receipt.getTotalAmount()));
//                contentStream.newLineAtOffset(60, 0);
//                contentStream.showText("%s".formatted(receipt.getCreatedAt().atZone(ZoneId.systemDefault()).format(fmt)));
//                contentStream.endText();
//
//                if (y < 100) {
//                    contentStream.close();
//
//                    PDPage newPage = new PDPage();
//                    document.addPage(newPage);
//
//                    contentStream = new PDPageContentStream(document, newPage);
//                    y = 750;
//                } else {
//                    y -= 18;
//                }
//
//            }
//
//            y -= 20;
//
//            contentStream.moveTo(margin, y);
//            contentStream.lineTo(550, y);
//            contentStream.stroke();
//            y -= 30;
//
//            contentStream.beginText();
//            contentStream.setFont(boldFont, 14);
//            contentStream.newLineAtOffset(margin, y);
//            contentStream.showText(
//                    "Gesamtbetrag: " +
//                            receipts.stream()
//                                    .map(Receipt::getTotalAmount)
//                                    .reduce(BigDecimal.ZERO, BigDecimal::add) +
//                            " EUR"
//            );
//            contentStream.endText();
//
//            contentStream.close();
//
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            document.save(out);
//
//            return new ByteArrayInputStream(out.toByteArray());
//
//        } catch (Exception e) {
//            return null;
//        }
//    }
}