package de.fhdw.vendix.store.core.persistance.receipt.port;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudCommandService;

interface ReceiptCommandService extends CrudCommandService<ReceiptDTO, Long> {}