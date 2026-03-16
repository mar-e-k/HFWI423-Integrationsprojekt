package de.fhdw.vendix.store.core.persistance.receipt_line;

import de.fhdw.vendix.commons.api.domain.receipt_line.port.ReceiptLineCommandPort;
import de.fhdw.vendix.commons.api.domain.receipt_line.port.ReceiptLineQueryPort;
import de.fhdw.vendix.commons.spring.core.crud.AbstractCrudLogAdapter;
import org.springframework.stereotype.Service;

@Service
class ReceiptLineAdapter extends AbstractCrudLogAdapter<ReceiptLine, Long> implements ReceiptLineCommandPort, ReceiptLineQueryPort {

    private final ReceiptLineRepository receiptLineRepository;
    private final ReceiptLineMapper receiptLineMapper;

    public ReceiptLineAdapter(ReceiptLineRepository receiptLineRepository, ReceiptLineMapper receiptLineMapper) {
        super(receiptLineRepository);
        this.receiptLineRepository = receiptLineRepository;
        this.receiptLineMapper = receiptLineMapper;
    }
}