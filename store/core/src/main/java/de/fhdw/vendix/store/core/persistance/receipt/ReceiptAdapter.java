package de.fhdw.vendix.store.core.persistance.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.port.ReceiptCommandPort;
import de.fhdw.vendix.commons.api.domain.receipt.port.ReceiptQueryPort;
import de.fhdw.vendix.commons.api.domain.receipt_line.dto.ReceiptLineDTO;
import de.fhdw.vendix.commons.spring.core.crud.AbstractDtoCrudAdapter;
import de.fhdw.vendix.store.core.persistance.receipt_line.ReceiptLineMapper;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
class ReceiptAdapter extends AbstractDtoCrudAdapter<Receipt, ReceiptDTO, Long> implements ReceiptQueryPort, ReceiptCommandPort {

    private final ReceiptEntityAdapter receiptEntityAdapter;
    private final ReceiptMapper receiptMapper;
    private final ReceiptLineMapper receiptLineMapper;

    ReceiptAdapter(ReceiptEntityAdapter receiptEntityAdapter, ReceiptMapper receiptMapper, ReceiptLineMapper receiptLineMapper) {
        super(receiptEntityAdapter, receiptMapper);
        this.receiptEntityAdapter = receiptEntityAdapter;
        this.receiptMapper = receiptMapper;
        this.receiptLineMapper = receiptLineMapper;
    }

    @Override
    public Set<ReceiptDTO> findAllByStoreId(Long storeID) {
        return receiptEntityAdapter.findAllByStoreId(storeID).stream()
                .map(receiptMapper::toDTO)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ReceiptDTO> findAllByRegisterId(Long registerID) {
        return receiptEntityAdapter.findAllByRegisterId(registerID).stream()
                .map(receiptMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<ReceiptDTO> findAllByCashierId(Long cashierID) {
        return receiptEntityAdapter.findAllByCashierId(cashierID).stream()
                .map(receiptMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<ReceiptDTO> findAllByStoreIdAndCreatedAtToday(Long storeID) {
        return receiptEntityAdapter.findAllByStoreIdAndCreatedAtToday(storeID).stream()
                .map(receiptMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<ReceiptLineDTO> findAllReceiptLinesByReceiptId(Long id) {
        return receiptEntityAdapter.findAllReceiptLinesByReceiptId(id).stream()
                .map(receiptLineMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
    }
}