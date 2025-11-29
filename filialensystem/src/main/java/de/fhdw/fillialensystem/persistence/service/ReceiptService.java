package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.Receipt;
import de.fhdw.fillialensystem.persistence.repository.ReceiptRepository;
import org.springframework.stereotype.Service;

@Service
public class ReceiptService extends AbstractCrudService<Receipt, Long> {

    public ReceiptService(ReceiptRepository receiptRepository) {
        super(receiptRepository);
    }
}
