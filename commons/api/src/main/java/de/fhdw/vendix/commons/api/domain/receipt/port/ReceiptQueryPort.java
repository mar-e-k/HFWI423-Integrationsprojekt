package de.fhdw.vendix.commons.api.domain.receipt.port;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt_line.dto.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.time.LocalDate;
import java.util.Set;

public interface ReceiptQueryPort extends QueryPort {
    Set<ReceiptDTO> findAllByDate(LocalDate date);

    Set<ReceiptDTO> findAllByStore(long storeID);

    Set<ReceiptDTO> findAllByStoreToday(long storeID);

    Set<ReceiptDTO> findAllByRegister(long registerID);

    Set<ReceiptDTO> findAllByCashier(long cashierID);

    Set<ReceiptLineDTO> findLinesForReceipt(long id);
}