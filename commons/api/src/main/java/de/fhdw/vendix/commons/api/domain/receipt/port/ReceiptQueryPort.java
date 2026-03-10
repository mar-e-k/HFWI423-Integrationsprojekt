package de.fhdw.vendix.commons.api.domain.receipt.port;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.time.LocalDate;
import java.util.List;

public interface ReceiptQueryPort extends QueryPort {
    List<ReceiptDTO> findAllByDate(LocalDate date);

    List<ReceiptDTO> findAllByStore(long storeID);

    List<ReceiptDTO> findAllByStoreToday(long storeID);

    List<ReceiptDTO> findAllByRegister(long registerID);

    List<ReceiptDTO> findAllByCashier(long cashierID);
}