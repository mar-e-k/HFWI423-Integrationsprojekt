package de.fhdw.vendix.store.core.domain.receipt_line;

import de.fhdw.vendix.commons.api.domain.receipt_line.port.ReceiptLineCommandPort;
import de.fhdw.vendix.commons.api.domain.receipt_line.port.ReceiptLineQueryPort;
import de.fhdw.vendix.commons.spring.core.crud.AbstractSpringDataCrudLogAdapter;
import org.springframework.stereotype.Service;

@Service
class ReceiptLineService extends AbstractSpringDataCrudLogAdapter<ReceiptLine, Long> implements ReceiptLineCommandPort, ReceiptLineQueryPort {

    private final ReceiptLineRepository receiptLineRepository;
    private final ReceiptLineMapper receiptLineMapper;

    public ReceiptLineService(ReceiptLineRepository receiptLineRepository, ReceiptLineMapper receiptLineMapper) {
        super(receiptLineRepository);
        this.receiptLineRepository = receiptLineRepository;
        this.receiptLineMapper = receiptLineMapper;
    }
}