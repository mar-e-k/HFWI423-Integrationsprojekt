package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.spring.data.persistance.crud.AbstractCrudService;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLineService;
import de.fhdw.vendix.store.core.domain.store_stock.StoreStockService;
import de.fhdw.vendix.store.core.domain.voucher.Voucher;
import de.fhdw.vendix.store.core.domain.voucher.VoucherService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
class ReceiptServiceImpl extends AbstractCrudService<Receipt, Long> implements ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptLineService receiptLineService;
    private final StoreStockService storeStockService;
    private final VoucherService voucherService;

    ReceiptServiceImpl(
            ReceiptRepository receiptRepository,
            ReceiptLineService receiptLineService,
            StoreStockService storeStockService,
            VoucherService voucherService
    ) {
        super(receiptRepository);
        this.receiptRepository = receiptRepository;
        this.receiptLineService = receiptLineService;
        this.storeStockService = storeStockService;
        this.voucherService = voucherService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Receipt> findAllByStoreId(Long storeId) {
        if (storeId == null || storeId <= 0) {
            return List.of();
        }
        return receiptRepository.findAllByStoreId(storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Receipt> findAllByRegisterId(Long registerId) {
        if (registerId == null || registerId <= 0) {
            return List.of();
        }
        return receiptRepository.findAllByRegisterId(registerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Receipt> findAllByCashierUuid(UUID cashierUuid) {
        if (cashierUuid == null) {
            return List.of();
        }
        return receiptRepository.findAllByCashierUuid(cashierUuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Receipt> findAllByStoreIdAndCreatedAtToday(Long storeId) {
        if (storeId == null || storeId <= 0) {
            return List.of();
        }
        return receiptRepository.findAllByStoreIdAndCreatedAtToday(storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findDistinctArticleIdsSoldTodayByStoreId(Long storeId) {
        if (storeId == null || storeId <= 0) {
            return List.of();
        }
        return receiptRepository.findDistinctArticleIdsSoldTodayByStoreId(storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceiptLine> findAllReceiptLinesByReceiptId(Long id) {
        if (id == null || id <= 0) {
            return List.of();
        }
        return receiptRepository.findAllReceiptLinesByReceiptId(id);
    }

    @Override
    @Transactional
    public Receipt checkoutReceipt(
            Receipt receipt,
            List<ReceiptLine> receiptLines,
            List<Voucher> receiptVouchers
    ) throws ReceiptAlreadyCheckedOutException {
        Objects.requireNonNull(receipt);
        Objects.requireNonNull(receiptLines);
        Objects.requireNonNull(receiptVouchers);

        Long receiptId = null;
        if (receipt.getId() != null) {
            throw new IllegalArgumentException("Receipt must not have an Id.");
        }
        if (receiptLines.isEmpty()) {
            throw new IllegalArgumentException("ReceiptLines must contain at least one item");
        }

        for (ReceiptLine receiptLine : receiptLines) {
            if (receiptId == null) {
                receiptId = receiptLine.getReceiptId();
            }
            if (!receiptLine.getReceiptId().equals(receiptId)) {
                throw new IllegalArgumentException(
                        "ReceiptLines contain differing receipt ids. Expected: '%d' Actual: '%d'."
                                .formatted(receiptId, receiptLine.getReceiptId())
                );
            }
        }


        Map<Long, Long> articlesAndAmounts = new HashMap<>();
        receiptLines.forEach(r ->
                articlesAndAmounts.merge(
                        r.getArticleId(),
                        r.getArticleAmount(),
                        Long::sum
                )
        );

        // TODO: fix id solution
        Receipt updated = new Receipt(
                receipt.getStoreId(),
                receipt.getRegisterId(),
                receipt.getCashierUuid(),
                receipt.getPaymentMethod(),
                receipt.getStatus()
        );

        Receipt created = super.create(updated);

        receiptLineService.createAll(receiptLines);
        storeStockService.decrementArticles(created.getStoreId(), articlesAndAmounts);

        receiptVouchers.forEach(Voucher::redeem);
        voucherService.updateAll(receiptVouchers);

        return created;
    }

    @Override
    @Transactional
    public Receipt cancelReceipt(Long id) throws ReceiptAlreadyCancelledException, ReceiptAlreadyPrintedException {
        Receipt receipt = super.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Receipt with ID '%d' not found.".formatted(id)
                ));

        Receipt cancelled = receipt.cancel();
        storeStockService.incrementArticles(receipt.getStoreId(), aggregateArticleAmounts(
                receiptRepository.findAllReceiptLinesByReceiptId(id)
        ));
        return super.update(cancelled);
    }

    @Override
    @Transactional
    public Receipt printReceipt(Long id) throws ReceiptAlreadyPrintedException, ReceiptAlreadyCancelledException {
        Receipt receipt = super.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Receipt with ID '%d' not found.".formatted(id)
                ));

        Receipt printed = receipt.print();
        return super.update(printed);
    }

    private static Map<Long, Long> aggregateArticleAmounts(List<ReceiptLine> lines) {
        Map<Long, Long> amountByArticle = new LinkedHashMap<>();
        for (ReceiptLine line : lines) {
            amountByArticle.merge(line.getArticleId(), line.getArticleAmount(), Long::sum);
        }
        return amountByArticle;
    }
}