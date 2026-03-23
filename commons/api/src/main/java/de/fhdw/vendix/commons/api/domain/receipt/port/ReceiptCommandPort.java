package de.fhdw.vendix.commons.api.domain.receipt.port;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.web.ReceiptCommandApi;
import de.fhdw.vendix.commons.api.structure.port.CommandPort;

public interface ReceiptCommandPort extends CommandPort, ReceiptCommandApi {

    ReceiptDTO create(ReceiptDTO entity);
}