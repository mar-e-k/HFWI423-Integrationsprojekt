package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReceiptMapper extends EntityMapper<Receipt, ReceiptDTO> {

    ReceiptMapper INSTANCE = Mappers.getMapper(ReceiptMapper.class);

    @Override
    ReceiptDTO toDTO(Receipt entity);

    @Override
    Receipt toEntity(ReceiptDTO receiptDTO);
}