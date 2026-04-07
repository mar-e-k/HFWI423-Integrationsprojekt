package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import de.fhdw.vendix.store.core.domain.register.RegisterMapper;
import de.fhdw.vendix.store.core.domain.store.StoreMapper;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {
                StoreMapper.class,
                RegisterMapper.class
        }
)
public interface ReceiptMapper extends EntityMapper<Receipt, ReceiptDTO> {

    @Override
    ReceiptDTO toDTO(Receipt entity);

    @Override
    Receipt toEntity(ReceiptDTO receiptDTO);

    @Override
    Set<ReceiptDTO> toDTOs(Iterable<Receipt> entities);

    @Override
    Set<Receipt> toEntities(Iterable<ReceiptDTO> receiptDTOS);
}