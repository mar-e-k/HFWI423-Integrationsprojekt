package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.port.ReceiptCommandPort;
import de.fhdw.vendix.commons.api.domain.receipt.port.ReceiptQueryPort;
import de.fhdw.vendix.commons.spring.core.crud.AbstractSpringDataCrudLogAdapter;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
class ReceiptAdapter extends AbstractSpringDataCrudLogAdapter<Receipt, Long> implements ReceiptCommandPort, ReceiptQueryPort {

    private final ReceiptRepository receiptRepository;
    private final ReceiptMapper receiptMapper;

    public ReceiptAdapter(ReceiptRepository receiptRepository, ReceiptMapper receiptMapper) {
        super(receiptRepository);
        this.receiptRepository = receiptRepository;
        this.receiptMapper = receiptMapper;
    }

    @Override
    public ReceiptDTO create(ReceiptRequestDTO dto) {
        return receiptMapper.toDTO(super.create(new Receipt(dto)));
    }

    // TODO

    @Override
    public List<ReceiptDTO> findAllByDate(LocalDate date) {
        return List.of();
    }

    @Override
    public List<ReceiptDTO> findAllByStore(long storeID) {
        return List.of();
    }

    @Override
    public List<ReceiptDTO> findAllByStoreToday(long storeID) {
        return List.of();
    }

    @Override
    public List<ReceiptDTO> findAllByRegister(long registerID) {
        return List.of();
    }

    @Override
    public List<ReceiptDTO> findAllByCashier(long cashierID) {
        return List.of();
    }
}