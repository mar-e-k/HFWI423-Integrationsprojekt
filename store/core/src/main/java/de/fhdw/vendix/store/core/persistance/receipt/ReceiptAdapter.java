package de.fhdw.vendix.store.core.persistance.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.port.ReceiptCommandPort;
import de.fhdw.vendix.commons.api.domain.receipt.port.ReceiptQueryPort;
import de.fhdw.vendix.commons.api.domain.receipt_line.dto.ReceiptLineDTO;
import de.fhdw.vendix.commons.spring.core.crud.AbstractSpringDataCrudLogAdapter;
import de.fhdw.vendix.store.core.persistance.receipt_line.ReceiptLineMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Service
class ReceiptAdapter extends AbstractSpringDataCrudLogAdapter<Receipt, Long> implements ReceiptCommandPort, ReceiptQueryPort {

    private final ReceiptRepository receiptRepository;
    private final ReceiptMapper receiptMapper;
    private final ReceiptLineMapper receiptLineMapper;

    public ReceiptAdapter(ReceiptRepository receiptRepository, ReceiptMapper receiptMapper, ReceiptLineMapper receiptLineMapper) {
        super(receiptRepository);
        this.receiptRepository = receiptRepository;
        this.receiptMapper = receiptMapper;
        this.receiptLineMapper = receiptLineMapper;
    }

    @Override
    public Set<ReceiptDTO> findAllByDate(LocalDate date) {
        return Set.of();
    }

    @Override
    public Set<ReceiptDTO> findAllByStore(long storeID) {
        return Set.of();
    }

    @Override
    public Set<ReceiptDTO> findAllByStoreToday(long storeID) {
        return Set.of();
    }

    @Override
    public Set<ReceiptDTO> findAllByRegister(long registerID) {
        return Set.of();
    }

    @Override
    public Set<ReceiptDTO> findAllByCashier(long cashierID) {
        return Set.of();
    }

    @Override
    public Set<ReceiptLineDTO> findLinesForReceipt(long id) {
        return receiptRepository.findLinesForReceipt(id).stream()
                .map(receiptLineMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public ReceiptDTO create(ReceiptDTO entity) {
        Receipt receipt = receiptMapper.toEntity(entity);
        Receipt created = super.create(receipt);
        return receiptMapper.toDTO(created);
    }
}