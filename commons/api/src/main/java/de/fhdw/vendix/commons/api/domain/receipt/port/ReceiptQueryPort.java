package de.fhdw.vendix.commons.api.domain.receipt.port;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt_line.dto.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.util.Set;

public interface ReceiptQueryPort extends QueryPort {
    Set<ReceiptDTO> findAllByStoreId(Long storeID);

    Set<ReceiptDTO> findAllByRegisterId(Long registerID);

    Set<ReceiptDTO> findAllByCashierId(Long cashierID);

    Set<ReceiptDTO> findAllByStoreIdAndCreatedAtToday(Long storeID);

    Set<ReceiptLineDTO> findAllReceiptLinesByReceiptId(Long id);
}