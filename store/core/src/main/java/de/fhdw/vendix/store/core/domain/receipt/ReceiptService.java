package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.structure.service.CrudService;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;

import java.util.Set;

public interface ReceiptService extends CrudService<Receipt, Long> {
    Set<Receipt> findAllByStoreId(Long storeId);

    Set<Receipt> findAllByRegisterId(Long registerId);

    Set<Receipt> findAllByCashierId(Long cashierId);

    Set<Receipt> findAllByStoreIdAndCreatedAtToday(Long storeId);

    Set<ReceiptLine> findAllReceiptLinesByReceiptId(Long id);
}