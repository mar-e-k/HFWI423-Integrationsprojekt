package de.fhdw.vendix.store.core.persistance.receipt;

import de.fhdw.vendix.commons.spring.core.crud.AbstractEntityCrudAdapter;
import de.fhdw.vendix.store.core.persistance.receipt_line.ReceiptLine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
class ReceiptEntityAdapter extends AbstractEntityCrudAdapter<Receipt, Long> {

    private final ReceiptRepository receiptRepository;

    ReceiptEntityAdapter(ReceiptRepository receiptRepository) {
        super(receiptRepository);
        this.receiptRepository = receiptRepository;
    }

    @Transactional(readOnly = true)
    public Set<Receipt> findAllByStoreId(Long storeId) {
        if (storeId == null || storeId <= 0) {
            return Set.of();
        }
        return receiptRepository.findAllByStoreId(storeId);
    }

    @Transactional(readOnly = true)
    public Set<Receipt> findAllByRegisterId(Long registerId) {
        if (registerId == null || registerId <= 0) {
            return Set.of();
        }
        return receiptRepository.findAllByRegisterId(registerId);
    }

    @Transactional(readOnly = true)
    public Set<Receipt> findAllByCashierId(Long cashierId) {
        if (cashierId == null || cashierId <= 0) {
            return Set.of();
        }
        return receiptRepository.findAllByCashierId(cashierId);
    }

    @Transactional(readOnly = true)
    public Set<Receipt> findAllByStoreIdAndCreatedAtToday(Long storeId) {
        if (storeId == null || storeId <= 0) {
            return Set.of();
        }
        return receiptRepository.findAllByStoreIdAndCreatedAtToday(storeId);
    }

    @Transactional(readOnly = true)
    public Set<ReceiptLine> findAllReceiptLinesByReceiptId(Long id) {
        if (id == null || id <= 0) {
            return Set.of();
        }
        return receiptRepository.findAllReceiptLinesByReceiptId(id);
    }
}