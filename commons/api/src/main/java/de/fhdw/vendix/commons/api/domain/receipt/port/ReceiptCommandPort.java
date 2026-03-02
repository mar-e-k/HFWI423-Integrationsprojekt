package de.fhdw.vendix.commons.api.domain.receipt.port;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptRequestDTO;
import de.fhdw.vendix.commons.api.structure.port.CommandPort;

public interface ReceiptCommandPort extends CommandPort {
    ReceiptDTO create(ReceiptRequestDTO dto);
    ReceiptDTO create(ReceiptDTO dto);
}