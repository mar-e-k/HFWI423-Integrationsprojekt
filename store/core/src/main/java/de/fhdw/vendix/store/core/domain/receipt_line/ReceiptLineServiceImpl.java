package de.fhdw.vendix.store.core.domain.receipt_line;

import de.fhdw.vendix.commons.spring.data.persistance.crud.AbstractCrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
class ReceiptLineServiceImpl extends AbstractCrudService<ReceiptLine, Long> implements ReceiptLineService {

    private final ReceiptLineBulkRepository receiptLineBulkRepository;

    ReceiptLineServiceImpl(ReceiptLineRepository receiptLineRepository, ReceiptLineBulkRepository receiptLineBulkRepository) {
        super(receiptLineRepository);
        this.receiptLineBulkRepository = receiptLineBulkRepository;
    }

    @Override
    @Transactional
    public List<ReceiptLine> createAll(Iterable<ReceiptLine> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Parameter 'entities' cannot be null");
        }

        List<ReceiptLine> lines = new ArrayList<>();
        for (ReceiptLine entity : entities) {
            if (entity == null) {
                throw new IllegalArgumentException("Parameter 'entities' contains null 'entity'");
            }
            if (entity.getId() != null) {
                throw new IllegalArgumentException("Parameter 'id' must be null");
            }
            beforeCreate(entity);
            lines.add(entity);
        }

        List<ReceiptLine> created = receiptLineBulkRepository.bulkInsert(lines);
        created.forEach(this::afterCreate);
        return created;
    }
}
