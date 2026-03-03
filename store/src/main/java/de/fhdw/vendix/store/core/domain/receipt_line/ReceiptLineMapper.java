package de.fhdw.vendix.store.core.domain.receipt_line;

import de.fhdw.vendix.commons.api.domain.receipt_line.dto.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReceiptLineMapper extends EntityMapper<ReceiptLine, ReceiptLineDTO> {

    ReceiptLineMapper INSTANCE = Mappers.getMapper(ReceiptLineMapper.class);

    @Override
    ReceiptLineDTO toDTO(ReceiptLine entity);

    @Override
    ReceiptLine toEntity(ReceiptLineDTO receiptLineDTO);
}