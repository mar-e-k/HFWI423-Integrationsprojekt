package de.fhdw.vendix.store.core.persistance.receipt_line;

import de.fhdw.vendix.commons.api.domain.receipt_line.dto.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.domain.receipt_line.port.ReceiptLineCommandPort;
import de.fhdw.vendix.commons.api.domain.receipt_line.port.ReceiptLineQueryPort;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import org.springframework.stereotype.Service;

@Service
class ReceiptLineAdapter extends AbstractDtoCrudAdapter<ReceiptLine, ReceiptLineDTO, Long> implements ReceiptLineQueryPort, ReceiptLineCommandPort {

    private final ReceiptLineEntityAdapter receiptLineEntityAdapter;
    private final ReceiptLineMapper receiptLineMapper;

    ReceiptLineAdapter(ReceiptLineEntityAdapter receiptLineEntityAdapter, ReceiptLineMapper receiptLineMapper) {
        super(receiptLineEntityAdapter, receiptLineMapper);
        this.receiptLineEntityAdapter = receiptLineEntityAdapter;
        this.receiptLineMapper = receiptLineMapper;
    }
}