package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.port.ReceiptCommandPort;
import de.fhdw.vendix.commons.api.domain.receipt.port.ReceiptQueryPort;
import de.fhdw.vendix.commons.spring.core.crud.AbstractSpringDataCrudLogAdapter;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Random;

@Service
public class ReceiptService extends AbstractSpringDataCrudLogAdapter<Receipt, Long> implements ReceiptCommandPort, ReceiptQueryPort {

    private final ReceiptRepository receiptRepository;
    private final ReceiptMapper receiptMapper;

    public ReceiptService(ReceiptRepository receiptRepository, ReceiptMapper receiptMapper) {
        super(receiptRepository);
        this.receiptRepository = receiptRepository;
        this.receiptMapper = receiptMapper;
    }

    // TODO: outlayer to new voucher service / class

//    @Transactional
//    public Receipt createReceipt(Long registerId, String uuid, List<ReceiptArticle> articles) {
//        Register register = registerRepository.findById(registerId)
//                .orElseThrow(EntityNotFoundException::new);
//
//        Account account = accountRepository.findByUuid(uuid)
//                .orElseThrow(EntityNotFoundException::new);
//
//        if (articles.isEmpty()) {
//            throw new IllegalArgumentException("No articles specified");
//        }
//
//        BigDecimal totalAmount = articles.stream()
//                .map(article -> article.getPrice().multiply(BigDecimal.valueOf(article.getAmount())))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        boolean isDepositOnly = articles.stream()
//                .allMatch(article -> article.getDepositStatus() == DepositStatus.EMPTY);
//
//        String depositRedemptionCode = null;
//        if (isDepositOnly) {
//            depositRedemptionCode = generateUniqueDepositRedemptionCode();
//        }
//
//        Receipt receipt = super.create(new Receipt(
//                register.getStore(),
//                register,
//                account,
//                totalAmount,
//                new ArrayList<>(),
//                isDepositOnly,
//                depositRedemptionCode));
//
//        List<ReceiptArticle> savedArticles = new ArrayList<>();
//        for (ReceiptArticle article : articles) {
//            savedArticles.add(receiptLinkArticleRepository.save(new ReceiptArticle(
//                    receipt,
//                    articleRepository.getReferenceById(article.getArticle().getId()),
//                    article.getPrice(),
//                    article.getAmount(),
//                    article.getTaxRate(),
//                    article.getOverridePrice(),
//                    article.getOverrideReason(),
//                    article.getDiscountedByPercent(),
//                    article.getDepositStatus())));
//        }
//
//        receipt.setReceiptArticles(savedArticles);
//        return receipt;
//    }

    // TODO: outlayer to new voucher service / class

//    @Transactional
//    public Receipt redeemDepositReceipt(String depositRedemptionCode) {
//        Receipt receipt = receiptRepository.findByDepositRedemptionCode(depositRedemptionCode)
//                .orElseThrow(() -> new EntityNotFoundException("Deposit receipt with code " + depositRedemptionCode + " not found."));
//
//        if (!receipt.isDepositOnly()) {
//            throw new IllegalArgumentException("Receipt is not a deposit-only receipt.");
//        }
//
//        if (redeemedDepositReceiptRepository.existsByReceiptId(receipt.getId())) {
//            throw new IllegalStateException("Deposit receipt has already been redeemed.");
//        }
//
//        redeemedDepositReceiptRepository.save(new RedeemedDepositReceipt(receipt, LocalDateTime.now()));
//
//        return receipt;
//    }

    // TODO: this must be done by a receiptVoucherService since it has to check for uniqueness
    private String generateUniqueDepositRedemptionCode() {
        String code;
        Random random = new Random();
        do {
            code = String.format("%05d", random.nextInt(100000));
        } while (receiptRepository.findByDepositRedemptionCode(code).isPresent());
        return code;
    }
}