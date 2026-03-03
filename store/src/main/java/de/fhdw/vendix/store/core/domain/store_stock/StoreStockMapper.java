package de.fhdw.vendix.store.core.domain.store_stock;

import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StoreStockMapper extends EntityMapper<StoreStock, StoreDTO> {

    StoreStockMapper INSTANCE = Mappers.getMapper(StoreStockMapper.class);

    @Override
    StoreDTO toDTO(StoreStock entity);

    @Override
    StoreStock toEntity(StoreDTO storeDTO);
}