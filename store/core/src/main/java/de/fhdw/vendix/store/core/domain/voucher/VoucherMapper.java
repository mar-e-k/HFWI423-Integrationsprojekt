package de.fhdw.vendix.store.core.domain.voucher;

import de.fhdw.vendix.commons.api.domain.voucher.VoucherDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface VoucherMapper extends EntityMapper<Voucher, VoucherDTO> {

    @Override
    VoucherDTO toDTO(Voucher entity);

    @Override
    Voucher toEntity(VoucherDTO voucherDTO);

    @Override
    List<VoucherDTO> toDTOs(Iterable<Voucher> entities);

    @Override
    List<Voucher> toEntities(Iterable<VoucherDTO> receiptVoucherDTOS);
}