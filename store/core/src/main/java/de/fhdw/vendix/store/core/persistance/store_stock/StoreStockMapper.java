package de.fhdw.vendix.store.core.persistance.store_stock;

import de.fhdw.vendix.commons.api.domain.store_stock.dto.StoreStockDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.store.core.persistance.EntityMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = EntityMapperConfig.class)
public interface StoreStockMapper extends EntityMapper<StoreStock, StoreStockDTO> {

    @Override
    StoreStockDTO toDTO(StoreStock entity);

    @Override
    StoreStock toEntity(StoreStockDTO storeStockDTO);
}