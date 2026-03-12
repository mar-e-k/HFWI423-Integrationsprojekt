package de.fhdw.vendix.store.core.persistance.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.store.core.persistance.EntityMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = EntityMapperConfig.class)
public interface ReceiptMapper extends EntityMapper<Receipt, ReceiptDTO> {

    @Override
    ReceiptDTO toDTO(Receipt entity);

    @Override
    Receipt toEntity(ReceiptDTO receiptDTO);
}