package de.fhdw.vendix.store.core.domain.receipt_line;

import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import de.fhdw.vendix.store.core.domain.receipt_line.service.ReceiptLineService;
import org.springframework.stereotype.Service;

@Service
class ReceiptLineAdapter extends AbstractDtoCrudAdapter<ReceiptLine, ReceiptLineDTO, Long> implements ReceiptLineService {

    private final ReceiptLineEntityAdapter receiptLineEntityAdapter;
    private final ReceiptLineMapper receiptLineMapper;

    ReceiptLineAdapter(ReceiptLineEntityAdapter receiptLineEntityAdapter, ReceiptLineMapper receiptLineMapper) {
        super(receiptLineEntityAdapter, receiptLineMapper);
        this.receiptLineEntityAdapter = receiptLineEntityAdapter;
        this.receiptLineMapper = receiptLineMapper;
    }
}