package de.fhdw.vendix.store.core.domain.store_stock_order;

import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.data.persistance.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface StoreStockOrderMapper extends EntityMapper<StoreStockOrder, StoreStockOrderDTO> {

    @Override
    StoreStockOrderDTO toDTO(StoreStockOrder entity);

    @Override
    StoreStockOrder toEntity(StoreStockOrderDTO dto);

    @Override
    List<StoreStockOrderDTO> toDTOs(Iterable<StoreStockOrder> entities);

    @Override
    List<StoreStockOrder> toEntities(Iterable<StoreStockOrderDTO> dtos);
}