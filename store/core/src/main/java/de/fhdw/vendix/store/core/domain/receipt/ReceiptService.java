package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.spring.data.persistance.service.CrudService;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;

import java.util.List;
import java.util.UUID;

public interface ReceiptService extends CrudService<Receipt, Long> {

    List<Receipt> findAllByStoreId(Long storeId);

    List<Receipt> findAllByRegisterId(Long registerId);

    List<Receipt> findAllByCashierUuid(UUID cashierUuid);

    List<Receipt> findAllByStoreIdAndCreatedAtToday(Long storeId);

    List<Long> findDistinctArticleIdsSoldTodayByStoreId(Long storeId);

    List<ReceiptLine> findAllReceiptLinesByReceiptId(Long id);

    /**
     * Storniert einen Bon (OPEN → CANCELLED).
     *
     * @throws ReceiptAlreadyCancelledException            wenn bereits storniert
     * @throws ReceiptAlreadyPrintedException              wenn bereits gedruckt
     * @throws jakarta.persistence.EntityNotFoundException wenn nicht gefunden
     */
    Receipt cancelReceipt(Long id) throws ReceiptAlreadyCancelledException, ReceiptAlreadyPrintedException;

    /**
     * Schließt einen Bon ab / druckt ihn (OPEN → PRINTED).
     *
     * @throws ReceiptAlreadyPrintedException              wenn bereits gedruckt
     * @throws ReceiptAlreadyCancelledException            wenn bereits storniert
     * @throws jakarta.persistence.EntityNotFoundException wenn nicht gefunden
     */
    Receipt printReceipt(Long id) throws ReceiptAlreadyPrintedException, ReceiptAlreadyCancelledException;
}
