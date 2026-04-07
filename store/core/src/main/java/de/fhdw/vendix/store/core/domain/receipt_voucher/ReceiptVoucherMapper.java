package de.fhdw.vendix.store.core.domain.receipt_voucher;

import de.fhdw.vendix.commons.api.domain.receipt_voucher.ReceiptVoucherDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptMapper;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {
                ReceiptMapper.class
        }
)
public interface ReceiptVoucherMapper extends EntityMapper<ReceiptVoucher, ReceiptVoucherDTO> {

    @Override
    ReceiptVoucherDTO toDTO(ReceiptVoucher entity);

    @Override
    ReceiptVoucher toEntity(ReceiptVoucherDTO receiptVoucherDTO);

    @Override
    Set<ReceiptVoucherDTO> toDTOs(Iterable<ReceiptVoucher> entities);

    @Override
    Set<ReceiptVoucher> toEntities(Iterable<ReceiptVoucherDTO> receiptVoucherDTOS);
}