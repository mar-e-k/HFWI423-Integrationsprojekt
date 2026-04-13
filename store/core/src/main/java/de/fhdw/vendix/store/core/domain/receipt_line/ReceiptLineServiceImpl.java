package de.fhdw.vendix.store.core.domain.receipt_line;

import de.fhdw.vendix.commons.spring.data.crud.AbstractCrudService;
import org.springframework.stereotype.Service;

@Service
class ReceiptLineServiceImpl extends AbstractCrudService<ReceiptLine, Long> implements ReceiptLineService {

    private final ReceiptLineRepository receiptLineRepository;

    ReceiptLineServiceImpl(ReceiptLineRepository receiptLineRepository) {
        super(receiptLineRepository);
        this.receiptLineRepository = receiptLineRepository;
    }
}