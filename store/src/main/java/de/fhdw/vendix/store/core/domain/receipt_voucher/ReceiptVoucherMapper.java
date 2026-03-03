package de.fhdw.vendix.store.core.domain.receipt_voucher;

import de.fhdw.vendix.commons.api.domain.receipt_voucher.dto.ReceiptVoucherDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReceiptVoucherMapper extends EntityMapper<ReceiptVoucher, ReceiptVoucherDTO> {

    ReceiptVoucherMapper INSTANCE = Mappers.getMapper(ReceiptVoucherMapper.class);

    @Override
    ReceiptVoucherDTO toDTO(ReceiptVoucher entity);

    @Override
    ReceiptVoucher toEntity(ReceiptVoucherDTO receiptVoucherDTO);
}