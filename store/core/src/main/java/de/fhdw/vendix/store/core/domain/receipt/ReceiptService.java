package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.structure.service.CrudService;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;

import java.util.Set;

public interface ReceiptService extends CrudService<Receipt, Long> {
    Set<Receipt> findAllByStoreId(Long storeID);

    Set<Receipt> findAllByRegisterId(Long registerID);

    Set<Receipt> findAllByCashierId(Long cashierID);

    Set<Receipt> findAllByStoreIdAndCreatedAtToday(Long storeID);

    Set<ReceiptLine> findAllReceiptLinesByReceiptId(Long id);
}