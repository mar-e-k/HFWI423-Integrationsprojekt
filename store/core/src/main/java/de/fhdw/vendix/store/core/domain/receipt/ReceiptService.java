package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.structure.service.CrudService;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;

import java.util.List;

public interface ReceiptService extends CrudService<Receipt, Long> {
    List<Receipt> findAllByStoreId(Long storeId);

    List<Receipt> findAllByRegisterId(Long registerId);

    List<Receipt> findAllByCashierId(Long cashierId);

    List<Receipt> findAllByStoreIdAndCreatedAtToday(Long storeId);

    List<ReceiptLine> findAllReceiptLinesByReceiptId(Long id);
}