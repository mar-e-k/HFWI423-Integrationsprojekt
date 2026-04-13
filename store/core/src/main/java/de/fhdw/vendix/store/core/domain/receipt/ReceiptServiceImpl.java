package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.spring.data.crud.AbstractCrudService;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
class ReceiptServiceImpl extends AbstractCrudService<Receipt, Long> implements ReceiptService {

    private final ReceiptRepository receiptRepository;

    ReceiptServiceImpl(ReceiptRepository receiptRepository) {
        super(receiptRepository);
        this.receiptRepository = receiptRepository;
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
    public List<Receipt> findAllByCashierId(Long cashierId) {
        if (cashierId == null || cashierId <= 0) {
            return List.of();
        }
        return receiptRepository.findAllByCashierId(cashierId);
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
    public List<ReceiptLine> findAllReceiptLinesByReceiptId(Long id) {
        if (id == null || id <= 0) {
            return List.of();
        }
        return receiptRepository.findAllReceiptLinesByReceiptId(id);
    }
}