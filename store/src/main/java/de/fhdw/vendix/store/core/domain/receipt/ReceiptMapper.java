package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.store.core.domain.account.AccountMapper;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLineMapper;
import de.fhdw.vendix.store.core.domain.receipt_voucher.ReceiptVoucher;
import de.fhdw.vendix.store.core.domain.receipt_voucher.ReceiptVoucherMapper;
import de.fhdw.vendix.store.core.domain.register.RegisterMapper;
import de.fhdw.vendix.store.core.domain.store.StoreMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {
        StoreMapper.class, RegisterMapper.class,
        AccountMapper.class, ReceiptLineMapper.class,
        ReceiptVoucherMapper.class
})
public interface ReceiptMapper extends EntityMapper<Receipt, ReceiptDTO> {

    ReceiptMapper INSTANCE = Mappers.getMapper(ReceiptMapper.class);

    @Override
    ReceiptDTO toDTO(Receipt entity);

    @Override
    Receipt toEntity(ReceiptDTO receiptDTO);
}