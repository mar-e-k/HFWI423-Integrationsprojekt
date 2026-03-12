package de.fhdw.vendix.store.core.persistance.receipt_voucher;

import de.fhdw.vendix.commons.api.domain.receipt_voucher.dto.ReceiptVoucherDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.store.core.persistance.EntityMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = EntityMapperConfig.class)
public interface ReceiptVoucherMapper extends EntityMapper<ReceiptVoucher, ReceiptVoucherDTO> {

    @Override
    ReceiptVoucherDTO toDTO(ReceiptVoucher entity);

    @Override
    ReceiptVoucher toEntity(ReceiptVoucherDTO receiptVoucherDTO);
}