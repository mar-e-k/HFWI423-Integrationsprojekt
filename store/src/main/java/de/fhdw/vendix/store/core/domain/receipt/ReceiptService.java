package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.core.api.dto.DepositStatus;
import de.fhdw.vendix.store.core.domain.AbstractCrudService;
import de.fhdw.vendix.store.core.domain.account.Account;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptArticle;
import de.fhdw.vendix.store.core.domain.register.Register;
import de.fhdw.vendix.store.core.domain.account.AccountRepository;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLinkArticleRepository;
import de.fhdw.vendix.store.core.domain.register.RegisterRepository;
import de.fhdw.vendix.store.core.domain.article.ArticleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ReceiptService extends AbstractCrudService<Receipt, Long> {

    private final ReceiptLinkArticleRepository receiptLinkArticleRepository;
    private final RegisterRepository registerRepository;
    private final AccountRepository accountRepository;
    private final ArticleRepository articleRepository;
    private final RedeemedDepositReceiptRepository redeemedDepositReceiptRepository;
    private final ReceiptRepository receiptRepository;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public ReceiptService(ReceiptRepository receiptRepository, ReceiptLinkArticleRepository receiptLinkArticleRepository, RegisterRepository registerRepository, AccountRepository accountRepository, ArticleRepository articleRepository, RedeemedDepositReceiptRepository redeemedDepositReceiptRepository) {
        super(receiptRepository);
        this.receiptLinkArticleRepository = receiptLinkArticleRepository;
        this.registerRepository = registerRepository;
        this.accountRepository = accountRepository;
        this.articleRepository = articleRepository;
        this.redeemedDepositReceiptRepository = redeemedDepositReceiptRepository;
        this.receiptRepository = receiptRepository;
    }

    public List<ReceiptArticle> getReceiptLinkArticles(Receipt receipt) {
        return receiptLinkArticleRepository.findByReceipt(receipt);
    }

    @Transactional
    public Receipt createReceipt(Long registerId, String uuid, List<ReceiptArticle> articles) {
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

        boolean isDepositOnly = articles.stream()
                .allMatch(article -> article.getDepositStatus() == DepositStatus.EMPTY);

        String depositRedemptionCode = null;
        if (isDepositOnly) {
            depositRedemptionCode = generateUniqueDepositRedemptionCode();
        }

        Receipt receipt = super.create(new Receipt(
                register.getStore(),
                register,
                account,
                totalAmount,
                new ArrayList<>(),
                isDepositOnly,
                depositRedemptionCode));

        List<ReceiptArticle> savedArticles = new ArrayList<>();
        for (ReceiptArticle article : articles) {
            savedArticles.add(receiptLinkArticleRepository.save(new ReceiptArticle(
                    receipt,
                    articleRepository.getReferenceById(article.getArticle().getId()),
                    article.getPrice(),
                    article.getAmount(),
                    article.getTaxRate(),
                    article.getOverridePrice(),
                    article.getOverrideReason(),
                    article.getDiscountedByPercent(),
                    article.getDepositStatus())));
        }

        receipt.setReceiptArticles(savedArticles);
        return receipt;
    }

    @Transactional
    public Receipt redeemDepositReceipt(String depositRedemptionCode) {
        Receipt receipt = receiptRepository.findByDepositRedemptionCode(depositRedemptionCode)
                .orElseThrow(() -> new EntityNotFoundException("Deposit receipt with code " + depositRedemptionCode + " not found."));

        if (!receipt.isDepositOnly()) {
            throw new IllegalArgumentException("Receipt is not a deposit-only receipt.");
        }

        if (redeemedDepositReceiptRepository.existsByReceiptId(receipt.getId())) {
            throw new IllegalStateException("Deposit receipt has already been redeemed.");
        }

        redeemedDepositReceiptRepository.save(new RedeemedDepositReceipt(receipt, LocalDateTime.now()));

        return receipt;
    }

    private String generateUniqueDepositRedemptionCode() {
        String code;
        Random random = new Random();
        do {
            code = String.format("%05d", random.nextInt(100000));
        } while (receiptRepository.findByDepositRedemptionCode(code).isPresent());
        return code;
    }

    public ByteArrayInputStream generateReceipt(Long receiptId, boolean isCashPayment) {
        Receipt receipt = super.findById(receiptId)
                .orElseThrow(EntityNotFoundException::new);
        return generateReceipt(receipt, isCashPayment);
    }

    public ByteArrayInputStream generateReceipt(Receipt receipt, boolean isCashPayment) {
        try {
            List<ReceiptArticle> articles = receiptLinkArticleRepository.findByReceipt(receipt);

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
            contentStream.showText("Datum: " + receipt.getCreatedAt().atZone(ZoneId.systemDefault()).format(fmt));
            contentStream.endText();
            y -= 20;

            contentStream.moveTo(margin, y);
            contentStream.lineTo(550, y);
            contentStream.stroke();
            y -= 20;

            contentStream.beginText();
            contentStream.setFont(boldFont, 12);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Filiale: %d %s-%s-%s".formatted(receipt.getStore().getId(), receipt.getStore().getCity(), receipt.getStore().getStreet(), receipt.getStore().getStreetNumber()));
            contentStream.endText();

            y -= 15;

            contentStream.beginText();
            contentStream.setFont(boldFont, 12);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Kasse: %d".formatted(receipt.getRegister().getId()));
            contentStream.endText();

            y -= 15;

            contentStream.beginText();
            contentStream.setFont(boldFont, 12);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Kassierer: %s %d".formatted(receipt.getAccount().getUsername(), receipt.getAccount().getId()));
            contentStream.endText();

            y -= 15;

            contentStream.beginText();
            contentStream.setFont(boldFont, 12);
            contentStream.newLineAtOffset(margin, y);
            if (receipt.getDepositRedemptionCode() == null) {
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

            for (int i = 0; i < articles.size(); i++) {
                ReceiptArticle article = articles.get(i);
                contentStream.beginText();
                contentStream.setFont(font, 10);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText(String.valueOf(i + 1));
                contentStream.newLineAtOffset(50, 0);
                if (article.getArticle().getName().length() > 25) {
                    contentStream.showText(article.getArticle().getName().substring(0, 22) + "...");
                } else {
                    contentStream.showText(article.getArticle().getName());
                }
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
            }

            contentStream.moveTo(margin, y);
            contentStream.lineTo(550, y);
            contentStream.stroke();
            y -= 30;

            contentStream.beginText();
            contentStream.setFont(boldFont, 14);
            contentStream.newLineAtOffset(350, y);
            contentStream.showText("Gesamtbetrag: " + String.format("%.2f EUR", receipt.getTotalAmount()));
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
            Map<Register, Map<Account, List<Receipt>>> grouped =
                    receipts.stream().collect(
                            Collectors.groupingBy(
                                    Receipt::getRegister,
                                    Collectors.groupingBy(Receipt::getAccount)
                            )
                    );

            Map<Register, Map<Account, BigDecimal>> totalsPerAccount =
                    grouped.entrySet().stream().collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().entrySet().stream().collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    acc -> acc.getValue().stream()
                                            .map(Receipt::getTotalAmount)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            ))
                    ));

            Map<Register, BigDecimal> totalsPerRegister =
                    grouped.entrySet().stream().collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().values().stream()
                                    .flatMap(List::stream)
                                    .map(Receipt::getTotalAmount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    ));

            Map<Account, Map<Register, BigDecimal>> totalsPerCashier = new HashMap<>();

            for (Map.Entry<Register, Map<Account, BigDecimal>> regEntry : totalsPerAccount.entrySet()) {
                Register register = regEntry.getKey();
                Map<Account, BigDecimal> accTotals = regEntry.getValue();

                for (Map.Entry<Account, BigDecimal> accEntry : accTotals.entrySet()) {
                    Account account = accEntry.getKey();
                    BigDecimal total = accEntry.getValue();

                    totalsPerCashier
                            .computeIfAbsent(account, a -> new HashMap<>())
                            .put(register, total);
                }
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
            y -= 25;

            contentStream.beginText();
            contentStream.setFont(boldFont, 14);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Filiale: " + receipts.getFirst().getStore().getId());
            contentStream.endText();
            y -= 30;

            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Datum: " + LocalDateTime.now().format(fmt));
            contentStream.endText();
            y -= 20;

            contentStream.moveTo(margin, y);
            contentStream.lineTo(550, y);
            contentStream.stroke();
            y -= 30;

            contentStream.beginText();
            contentStream.setFont(boldFont, 14);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Beleganzahl: %d".formatted(receipts.size()));
            contentStream.endText();
            y -= 20;

            contentStream.beginText();
            contentStream.setFont(boldFont, 14);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Kontoanzahl: %d".formatted(totalsPerAccount.values().stream()
                    .map(Map::keySet)
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet())
                    .size()));
            contentStream.endText();
            y -= 20;

            contentStream.beginText();
            contentStream.setFont(boldFont, 14);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Kassenanzahl: %d".formatted(totalsPerRegister.size()));
            contentStream.endText();
            y -= 20;

            contentStream.moveTo(margin, y);
            contentStream.lineTo(550, y);
            contentStream.stroke();
            y -= 30;

            for (Register register : totalsPerRegister.keySet()) {
                BigDecimal registerTotal = totalsPerRegister.get(register);

                contentStream.beginText();
                contentStream.setFont(boldFont, 14);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Kasse " + register.getId() + ": " + registerTotal + " EUR");
                contentStream.endText();
                y -= 20;

                Map<Account, BigDecimal> accountTotals = totalsPerAccount.get(register);

                for (Map.Entry<Account, BigDecimal> accEntry : accountTotals.entrySet()) {
                    contentStream.beginText();
                    contentStream.setFont(font, 12);
                    contentStream.newLineAtOffset(margin + 30, y);
                    contentStream.showText("Kassierer " + accEntry.getKey().getUsername() + ": " + accEntry.getValue() + " EUR");
                    contentStream.endText();
                    y -= 18;
                }

                y -= 10;
            }

            contentStream.moveTo(margin, y);
            contentStream.lineTo(550, y);
            contentStream.stroke();
            y -= 30;

            for (Map.Entry<Account, Map<Register, BigDecimal>> cashierEntry : totalsPerCashier.entrySet()) {

                Account account = cashierEntry.getKey();
                Map<Register, BigDecimal> perRegister = cashierEntry.getValue();

                contentStream.beginText();
                contentStream.setFont(boldFont, 14);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText(
                        "Kassierer " + account.getUsername() + ": " +
                                String.format("%.2f EUR",
                                        perRegister.values().stream()
                                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                );
                contentStream.endText();
                y -= 20;

                for (Map.Entry<Register, BigDecimal> regEntry : perRegister.entrySet()) {

                    contentStream.beginText();
                    contentStream.setFont(font, 12);
                    contentStream.newLineAtOffset(margin + 30, y);
                    contentStream.showText(
                            "Kasse " + regEntry.getKey().getId() + ": " +
                                    String.format("%.2f EUR", regEntry.getValue())
                    );
                    contentStream.endText();
                    y -= 18;
                }

                y -= 10;
            }


            contentStream.moveTo(margin, y);
            contentStream.lineTo(550, y);
            contentStream.stroke();
            y -= 30;

            contentStream.beginText();
            contentStream.setFont(boldFont, 12);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Beleg-ID");
            contentStream.newLineAtOffset(70, 0);
            contentStream.showText("Beleg-Art");
            contentStream.newLineAtOffset(90, 0);
            contentStream.showText("Kasse");
            contentStream.newLineAtOffset(70, 0);
            contentStream.showText("Kassierer");
            contentStream.newLineAtOffset(90, 0);
            contentStream.showText("Preis");
            contentStream.newLineAtOffset(60, 0);
            contentStream.showText("Datum");
            contentStream.endText();
            y -= 20;

            for (Receipt receipt : receipts) {
                contentStream.beginText();
                contentStream.setFont(font, 11);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("%d".formatted(receipt.getId()));
                contentStream.newLineAtOffset(70, 0);
                if (receipt.isDepositOnly()) {
                    contentStream.showText("PFAND");
                } else {
                    contentStream.showText("BELEG");
                }
                contentStream.newLineAtOffset(90, 0);
                contentStream.showText("%d".formatted(receipt.getRegister().getId()));
                contentStream.newLineAtOffset(70, 0);
                contentStream.showText("%s".formatted(receipt.getAccount().getUsername()));
                contentStream.newLineAtOffset(90, 0);
                contentStream.showText("%s".formatted(receipt.getTotalAmount()));
                contentStream.newLineAtOffset(60, 0);
                contentStream.showText("%s".formatted(receipt.getCreatedAt().atZone(ZoneId.systemDefault()).format(fmt)));
                contentStream.endText();

                if (y < 100) {
                    contentStream.close();

                    PDPage newPage = new PDPage();
                    document.addPage(newPage);

                    contentStream = new PDPageContentStream(document, newPage);
                    y = 750;
                } else {
                    y -= 18;
                }

            }

            y -= 20;

            contentStream.moveTo(margin, y);
            contentStream.lineTo(550, y);
            contentStream.stroke();
            y -= 30;

            contentStream.beginText();
            contentStream.setFont(boldFont, 14);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText(
                    "Gesamtbetrag: " +
                            receipts.stream()
                                    .map(Receipt::getTotalAmount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add) +
                            " EUR"
            );
            contentStream.endText();

            contentStream.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            return null;
        }
    }
}
