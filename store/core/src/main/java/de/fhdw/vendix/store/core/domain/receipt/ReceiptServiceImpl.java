package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.spring.data.crud.AbstractCrudService;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;
import jakarta.persistence.EntityNotFoundException;
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

    // ─── Bestehende Methoden ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<Receipt> findAllByStoreId(Long storeId) {
        if (storeId == null || storeId <= 0) return List.of();
        return receiptRepository.findAllByStoreId(storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Receipt> findAllByRegisterId(Long registerId) {
        if (registerId == null || registerId <= 0) return List.of();
        return receiptRepository.findAllByRegisterId(registerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Receipt> findAllByCashierId(Long cashierId) {
        if (cashierId == null || cashierId <= 0) return List.of();
        return receiptRepository.findAllByCashierId(cashierId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Receipt> findAllByStoreIdAndCreatedAtToday(Long storeId) {
        if (storeId == null || storeId <= 0) return List.of();
        return receiptRepository.findAllByStoreIdAndCreatedAtToday(storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceiptLine> findAllReceiptLinesByReceiptId(Long id) {
        if (id == null || id <= 0) return List.of();
        return receiptRepository.findAllReceiptLinesByReceiptId(id);
    }

    // ─── Stornierung ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Receipt cancelReceipt(Long id)
            throws ReceiptAlreadyCancelledException, ReceiptAlreadyPrintedException {

        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Bon mit ID " + id + " nicht gefunden."));

        Receipt cancelled = receipt.cancel();
        return receiptRepository.save(cancelled);
    }

    // ─── Bondruck ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Receipt printReceipt(Long id)
            throws ReceiptAlreadyPrintedException, ReceiptAlreadyCancelledException {

        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Bon mit ID " + id + " nicht gefunden."));

        Receipt printed = receipt.print();
        return receiptRepository.save(printed);
    }
}