package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.spring.data.persistance.service.CrudService;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;
import de.fhdw.vendix.store.core.domain.voucher.Voucher;

import java.util.List;
import java.util.UUID;

public interface ReceiptService extends CrudService<Receipt, Long> {

    List<Receipt> findAllByStoreId(Long storeId);

    List<Receipt> findAllByRegisterId(Long registerId);

    List<Receipt> findAllByCashierUuid(UUID cashierUuid);

    List<Receipt> findAllByStoreIdAndCreatedAtToday(Long storeId);

    List<Long> findDistinctArticleIdsSoldTodayByStoreId(Long storeId);

    List<ReceiptLine> findAllReceiptLinesByReceiptId(Long id);

    Receipt checkoutReceipt(
            Receipt receipt,
            List<ReceiptLine> receiptLines,
            List<Voucher> receiptVouchers
    ) throws ReceiptAlreadyCheckedOutException;

    Receipt cancelReceipt(Long id) throws ReceiptAlreadyCancelledException, ReceiptAlreadyPrintedException;

    Receipt printReceipt(Long id) throws ReceiptAlreadyPrintedException, ReceiptAlreadyCancelledException;
}