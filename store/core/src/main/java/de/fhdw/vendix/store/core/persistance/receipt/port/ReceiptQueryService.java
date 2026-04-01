package de.fhdw.vendix.store.core.persistance.receipt.port;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudQueryService;

import java.util.Set;

interface ReceiptQueryService extends CrudQueryService<ReceiptDTO, Long> {
    Set<ReceiptDTO> findAllByStoreId(Long storeID);

    Set<ReceiptDTO> findAllByRegisterId(Long registerID);

    Set<ReceiptDTO> findAllByCashierId(Long cashierID);

    Set<ReceiptDTO> findAllByStoreIdAndCreatedAtToday(Long storeID);

    Set<ReceiptLineDTO> findAllReceiptLinesByReceiptId(Long id);
}