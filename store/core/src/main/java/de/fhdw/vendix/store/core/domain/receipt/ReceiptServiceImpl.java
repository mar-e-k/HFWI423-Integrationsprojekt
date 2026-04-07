package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
class ReceiptServiceImpl extends AbstractEntityCrudAdapter<Receipt, Long> implements ReceiptService {

    private final ReceiptRepository receiptRepository;

    ReceiptServiceImpl(ReceiptRepository receiptRepository) {
        super(receiptRepository);
        this.receiptRepository = receiptRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Receipt> findAllByStoreId(Long storeId) {
        if (storeId == null || storeId <= 0) {
            return Set.of();
        }
        return receiptRepository.findAllByStoreId(storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Receipt> findAllByRegisterId(Long registerId) {
        if (registerId == null || registerId <= 0) {
            return Set.of();
        }
        return receiptRepository.findAllByRegisterId(registerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Receipt> findAllByCashierId(Long cashierId) {
        if (cashierId == null || cashierId <= 0) {
            return Set.of();
        }
        return receiptRepository.findAllByCashierId(cashierId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Receipt> findAllByStoreIdAndCreatedAtToday(Long storeId) {
        if (storeId == null || storeId <= 0) {
            return Set.of();
        }
        return receiptRepository.findAllByStoreIdAndCreatedAtToday(storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<ReceiptLine> findAllReceiptLinesByReceiptId(Long id) {
        if (id == null || id <= 0) {
            return Set.of();
        }
        return receiptRepository.findAllReceiptLinesByReceiptId(id);
    }
}