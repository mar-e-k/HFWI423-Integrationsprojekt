package de.fhdw.vendix.store.core.persistance.receipt_voucher;

import de.fhdw.vendix.commons.api.domain.receipt_voucher.ReceiptVoucherDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import de.fhdw.vendix.store.core.persistance.receipt.ReceiptMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {ReceiptMapper.class}
)
public interface ReceiptVoucherMapper extends EntityMapper<ReceiptVoucher, ReceiptVoucherDTO> {

    @Override
    ReceiptVoucherDTO toDTO(ReceiptVoucher entity);

    @Override
    ReceiptVoucher toEntity(ReceiptVoucherDTO receiptVoucherDTO);

    @Override
    List<ReceiptVoucherDTO> toDTOs(Iterable<ReceiptVoucher> entities);

    @Override
    List<ReceiptVoucher> toEntities(Iterable<ReceiptVoucherDTO> receiptVoucherDTOS);
}