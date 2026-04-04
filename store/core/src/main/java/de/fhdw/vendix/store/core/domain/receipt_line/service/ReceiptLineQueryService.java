package de.fhdw.vendix.store.core.domain.receipt_line.service;

import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudQueryService;

interface ReceiptLineQueryService extends CrudQueryService<ReceiptLineDTO, Long> {}