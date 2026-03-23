package de.fhdw.vendix.store.core.persistance.receipt_line;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import org.springframework.stereotype.Service;

@Service
class ReceiptLineEntityAdapter extends AbstractEntityCrudAdapter<ReceiptLine, Long> {

    private final ReceiptLineRepository receiptLineRepository;

    ReceiptLineEntityAdapter(ReceiptLineRepository receiptLineRepository) {
        super(receiptLineRepository);
        this.receiptLineRepository = receiptLineRepository;
    }
}