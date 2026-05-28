package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.spring.data.persistance.crud.AbstractCrudService;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;
import de.fhdw.vendix.store.core.domain.store_stock.StoreStockService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
class ReceiptServiceImpl extends AbstractCrudService<Receipt, Long> implements ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final StoreStockService storeStockService;

    ReceiptServiceImpl(ReceiptRepository receiptRepository, StoreStockService storeStockService) {
        super(receiptRepository);
        this.receiptRepository = receiptRepository;
        this.storeStockService = storeStockService;
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
    public Receipt cancelReceipt(Long id) throws ReceiptAlreadyCancelledException, ReceiptAlreadyPrintedException {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Receipt with ID '%d' not found.".formatted(id)
                ));

        Receipt cancelled = receipt.cancel();
        storeStockService.incrementArticles(receipt.getStoreId(), aggregateArticleAmounts(
                receiptRepository.findAllReceiptLinesByReceiptId(id)
        ));
        return receiptRepository.save(cancelled);
    }

    @Override
    @Transactional
    public Receipt printReceipt(Long id) throws ReceiptAlreadyPrintedException, ReceiptAlreadyCancelledException {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Receipt with ID '%d' not found.".formatted(id)
                ));

        Receipt printed = receipt.print();
        return receiptRepository.save(printed);
    }

    private static Map<Long, Long> aggregateArticleAmounts(List<ReceiptLine> lines) {
        Map<Long, Long> amountByArticle = new LinkedHashMap<>();
        for (ReceiptLine line : lines) {
            amountByArticle.merge(line.getArticleId(), line.getArticleAmount(), Long::sum);
        }
        return amountByArticle;
    }
}