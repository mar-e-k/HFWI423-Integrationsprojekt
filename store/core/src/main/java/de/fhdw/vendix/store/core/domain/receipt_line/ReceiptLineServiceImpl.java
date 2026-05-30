package de.fhdw.vendix.store.core.domain.receipt_line;

import de.fhdw.vendix.commons.spring.data.persistance.crud.AbstractCrudService;
import org.springframework.stereotype.Service;

@Service
class ReceiptLineServiceImpl extends AbstractCrudService<ReceiptLine, Long> implements ReceiptLineService {

    private final ReceiptLineRepository receiptLineRepository;
    private final ReceiptLineBulkRepository receiptLineBulkRepository;

    ReceiptLineServiceImpl(ReceiptLineRepository receiptLineRepository, ReceiptLineBulkRepository receiptLineBulkRepository) {
        super(receiptLineRepository);
        this.receiptLineRepository = receiptLineRepository;
        this.receiptLineBulkRepository = receiptLineBulkRepository;
    }
}