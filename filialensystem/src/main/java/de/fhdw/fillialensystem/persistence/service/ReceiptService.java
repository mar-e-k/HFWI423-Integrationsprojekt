package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.commons.api.dto.DepositStatus;
import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.entity.Receipt;
import de.fhdw.fillialensystem.persistence.entity.ReceiptLinkArticle;
import de.fhdw.fillialensystem.persistence.entity.RedeemedDepositReceipt;
import de.fhdw.fillialensystem.persistence.entity.Register;
import de.fhdw.fillialensystem.persistence.repository.AccountRepository;
import de.fhdw.fillialensystem.persistence.repository.ReceiptLinkArticleRepository;
import de.fhdw.fillialensystem.persistence.repository.ReceiptRepository;
import de.fhdw.fillialensystem.persistence.repository.RedeemedDepositReceiptRepository;
import de.fhdw.fillialensystem.persistence.repository.RegisterRepository;
import de.fhdw.fillialensystem.persistence.repository.imported.ArticleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final ReceiptRepository receiptRepository; // Inject ReceiptRepository

    public ReceiptService(ReceiptRepository receiptRepository, ReceiptLinkArticleRepository receiptLinkArticleRepository, RegisterRepository registerRepository, AccountRepository accountRepository, ArticleRepository articleRepository, RedeemedDepositReceiptRepository redeemedDepositReceiptRepository) {
        super(receiptRepository);
        this.receiptLinkArticleRepository = receiptLinkArticleRepository;
        this.registerRepository = registerRepository;
        this.accountRepository = accountRepository;
        this.articleRepository = articleRepository;
        this.redeemedDepositReceiptRepository = redeemedDepositReceiptRepository;
        this.receiptRepository = receiptRepository; // Initialize ReceiptRepository
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

        // Determine if the receipt is deposit-only
        boolean isDepositOnly = articles.stream()
                .allMatch(article -> article.getDepositStatus() == DepositStatus.EMPTY);

        String depositRedemptionCode = null;
        if (isDepositOnly) {
            depositRedemptionCode = generateUniqueDepositRedemptionCode();
        }

        Receipt receipt = super.save(new Receipt(
                register.getStore(),
                register,
                account,
                totalAmount,
                new ArrayList<>(), // Initialisiere mit leerer Liste
                isDepositOnly,
                depositRedemptionCode)); // Füge den neuen Parameter hinzu

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
                    article.getDiscountedByPercent(),
                    article.getDepositStatus()))); // DepositStatus hinzugefügt
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
}
