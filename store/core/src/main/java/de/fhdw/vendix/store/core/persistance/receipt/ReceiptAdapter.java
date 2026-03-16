package de.fhdw.vendix.store.core.persistance.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.port.ReceiptCommandPort;
import de.fhdw.vendix.commons.api.domain.receipt.port.ReceiptQueryPort;
import de.fhdw.vendix.commons.api.domain.receipt_line.dto.ReceiptLineDTO;
import de.fhdw.vendix.commons.spring.core.crud.AbstractCrudLogAdapter;
import de.fhdw.vendix.store.core.persistance.receipt_line.ReceiptLineMapper;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
class ReceiptAdapter extends AbstractCrudLogAdapter<Receipt, Long> implements ReceiptCommandPort, ReceiptQueryPort {

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
    public Set<ReceiptDTO> findAllByStoreId(Long storeID) {
        return receiptRepository.findAllByStoreId(storeID).stream()
                .map(receiptMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<ReceiptDTO> findAllByRegisterId(Long registerID) {
        return receiptRepository.findAllByRegisterId(registerID).stream()
                .map(receiptMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<ReceiptDTO> findAllByCashierId(Long cashierID) {
        return receiptRepository.findAllByCashierId(cashierID).stream()
                .map(receiptMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<ReceiptDTO> findAllByStoreIdAndCreatedAtToday(Long storeID) {
        return receiptRepository.findAllByStoreIdAndCreatedAtToday(storeID).stream()
                .map(receiptMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<ReceiptLineDTO> findAllReceiptLinesByReceiptId(Long id) {
        return receiptRepository.findAllReceiptLinesByReceiptId(id).stream()
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