package de.fhdw.vendix.store.core.persistance.receipt.port;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.api.structure.service.CommandService;

interface ReceiptCommandService extends CommandService {
    ReceiptDTO create(ReceiptDTO entity);
}