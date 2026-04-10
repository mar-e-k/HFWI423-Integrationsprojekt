package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface ReceiptMapper extends EntityMapper<Receipt, ReceiptDTO> {

    @Override
    ReceiptDTO toDTO(Receipt entity);

    @Override
    Receipt toEntity(ReceiptDTO receiptDTO);

    @Override
    List<ReceiptDTO> toDTOs(Iterable<Receipt> entities);

    @Override
    List<Receipt> toEntities(Iterable<ReceiptDTO> receiptDTOS);
}