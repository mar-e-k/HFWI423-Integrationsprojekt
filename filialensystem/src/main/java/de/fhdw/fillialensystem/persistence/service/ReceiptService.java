package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.entity.Receipt;
import de.fhdw.fillialensystem.persistence.entity.ReceiptLinkArticle;
import de.fhdw.fillialensystem.persistence.entity.Register;
import de.fhdw.fillialensystem.persistence.entity.imported.Article;
import de.fhdw.fillialensystem.persistence.repository.AccountRepository;
import de.fhdw.fillialensystem.persistence.repository.ReceiptLinkArticleRepository;
import de.fhdw.fillialensystem.persistence.repository.ReceiptRepository;
import de.fhdw.fillialensystem.persistence.repository.RegisterRepository;
import de.fhdw.fillialensystem.persistence.repository.imported.ArticleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReceiptService extends AbstractCrudService<Receipt, Long> {

    private final ReceiptLinkArticleRepository receiptLinkArticleRepository;
    private final RegisterRepository registerRepository;
    private final AccountRepository accountRepository;
    private final ArticleRepository articleRepository;

    public ReceiptService(ReceiptRepository receiptRepository, ReceiptLinkArticleRepository receiptLinkArticleRepository, RegisterRepository registerRepository, AccountRepository accountRepository, ArticleRepository articleRepository) {
        super(receiptRepository);
        this.receiptLinkArticleRepository = receiptLinkArticleRepository;
        this.registerRepository = registerRepository;
        this.accountRepository = accountRepository;
        this.articleRepository = articleRepository;
    }

    public List<ReceiptLinkArticle> getReceiptLinkArticles(Receipt receipt) {
        return receiptLinkArticleRepository.findByReceipt(receipt);
    }

    @Transactional
    public Receipt createReceipt(Long registerId, String uuid, List<ReceiptLinkArticle> articles) {
        Register register = registerRepository.findById(registerId)
                .orElseThrow(EntityNotFoundException::new);

        Account account = accountRepository.findByUuid(uuid)
                .orElseThrow(EntityNotFoundException::new);

        if (articles.isEmpty()) {
            throw new IllegalArgumentException("No articles specified");
        }

        BigDecimal totalAmount = articles.stream()
                .map(article -> article.getPrice().multiply(BigDecimal.valueOf(article.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Receipt receipt = super.save(new Receipt(
                register.getStore(),
                register,
                account,
                totalAmount,
                null));

        List<ReceiptLinkArticle> savedArticles = new ArrayList<>();
        for (ReceiptLinkArticle article : articles) {
            savedArticles.add(receiptLinkArticleRepository.save(new ReceiptLinkArticle(
                    receipt,
                    articleRepository.getReferenceById(article.getArticle().getId()),
                    article.getPrice(),
                    article.getAmount(),
                    article.getTaxRate(),
                    article.getOverridePrice(),
                    article.getOverrideReason(),
                    article.getDiscountedByPercent())));
        }

        receipt.setReceiptArticles(savedArticles);
        return receipt;
    }

    public ByteArrayInputStream generateReceipt(Long receiptId, boolean isCashPayment) {
        Receipt receipt = super.findById(receiptId)
                .orElseThrow(EntityNotFoundException::new);
        return generateReceipt(receipt, isCashPayment);
    }


    public ByteArrayInputStream generateReceipt(Receipt receipt, boolean isCashPayment) {
        try {
            List<ReceiptLinkArticle> articles = receiptLinkArticleRepository.findByReceipt(receipt);

            PDDocument document = new PDDocument();
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
            contentStream.showText("Kaufbeleg");
            contentStream.endText();
            y -= 30;

            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Datum: " + receipt.getCreatedAt().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
            contentStream.endText();
            y -= 20;

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

            for (int i = 0; i < articles.size(); i++) {
                ReceiptLinkArticle article = articles.get(i);
                contentStream.beginText();
                contentStream.setFont(font, 10);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText(String.valueOf(i + 1));
                contentStream.newLineAtOffset(50, 0);
                contentStream.showText(article.getArticle().getName());
                contentStream.newLineAtOffset(150, 0);
                contentStream.showText(String.valueOf(article.getAmount()));
                contentStream.newLineAtOffset(70, 0);
                contentStream.showText(String.format("%d %%", article.getArticle().getTaxRatePercent().intValue()));
                contentStream.newLineAtOffset(100, 0);
                if (article.getOverridePrice() != null && article.getOverridePrice().compareTo(BigDecimal.ZERO) > 0) {
                    contentStream.showText(String.format("%.2f EUR", article.getOverridePrice().doubleValue()));
                    contentStream.newLineAtOffset(80, 0);
                } else {
                    contentStream.showText(String.format("%.2f EUR", article.getPrice().doubleValue()));
                    contentStream.newLineAtOffset(80, 0);
                }
                if (article.getDiscountedByPercent() != null && article.getDiscountedByPercent().compareTo(BigDecimal.ZERO) > 0) {
                    contentStream.showText(String.format("%d %% x %d", article.getDiscountedByPercent().intValue(), article.getAmount()));
                } else {
                    contentStream.showText("Kein Rabatt");
                }
                contentStream.endText();
                y -= 20;

                total = total.add(calcTrueSum(article));
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

            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(50, y);
            String paymentText = isCashPayment ? "Bargeldzahlung" : "Kartenzahlung";
            contentStream.showText(paymentText);
            contentStream.endText();
            y -= 30;

            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Vielen Dank für Ihren Einkauf!");
            contentStream.endText();

            contentStream.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);

            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    public ByteArrayInputStream generateDailyReceipt(List<Receipt> receipts) {
        try {
            List<ReceiptLinkArticle> articles = new ArrayList<ReceiptLinkArticle>();
            for (Receipt receipt : receipts) {
                articles.addAll(getReceiptLinkArticles(receipt));
            }

            PDDocument document = new PDDocument();
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
            contentStream.showText("Tagesabschluss");
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

            for (int i = 0; i < articles.size(); i++) {
                ReceiptLinkArticle article = articles.get(i);
                contentStream.beginText();
                contentStream.setFont(font, 10);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText(String.valueOf(i + 1));
                contentStream.newLineAtOffset(50, 0);
                contentStream.showText(article.getArticle().getName());
                contentStream.newLineAtOffset(150, 0);
                contentStream.showText(String.valueOf(article.getAmount()));
                contentStream.newLineAtOffset(70, 0);
                contentStream.showText(String.format("%d %%", article.getArticle().getTaxRatePercent().intValue()));
                contentStream.newLineAtOffset(100, 0);
                if (article.getOverridePrice() != null && article.getOverridePrice().compareTo(BigDecimal.ZERO) > 0) {
                    contentStream.showText(String.format("%.2f EUR", article.getOverridePrice().doubleValue()));
                    contentStream.newLineAtOffset(80, 0);
                } else {
                    contentStream.showText(String.format("%.2f EUR", article.getPrice().doubleValue()));
                    contentStream.newLineAtOffset(80, 0);
                }
                if (article.getDiscountedByPercent() != null && article.getDiscountedByPercent().compareTo(BigDecimal.ZERO) > 0) {
                    contentStream.showText(String.format("%d %% x %d", article.getDiscountedByPercent().intValue(), article.getAmount()));
                } else {
                    contentStream.showText("Kein Rabatt");
                }
                contentStream.endText();
                y -= 20;

                total = total.add(calcTrueSum(article));
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

            contentStream.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);

            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    @NotNull
    private BigDecimal calcTrueSum(ReceiptLinkArticle receiptLinkArticle) {
        BigDecimal basePrice = (receiptLinkArticle.getOverridePrice() != null
                && receiptLinkArticle.getOverridePrice().compareTo(BigDecimal.ZERO) > 0)
                ? receiptLinkArticle.getOverridePrice()
                : receiptLinkArticle.getPrice();

        BigDecimal discountMultiplier = BigDecimal.ONE;
        if (receiptLinkArticle.getDiscountedByPercent() != null &&
                receiptLinkArticle.getDiscountedByPercent().compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal percent = receiptLinkArticle.getDiscountedByPercent();
            discountMultiplier = BigDecimal.ONE.subtract(
                    percent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            );
        }

        BigDecimal lineTotal = basePrice
                .multiply(BigDecimal.valueOf(receiptLinkArticle.getAmount()))
                .multiply(discountMultiplier)
                .setScale(2, RoundingMode.HALF_UP);

        return lineTotal;
    }
}
