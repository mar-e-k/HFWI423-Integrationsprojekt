package de.fhdw.vendix.store.core.domain.store;

import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StoreMapper extends EntityMapper<Store, StoreDTO> {

    StoreMapper INSTANCE = Mappers.getMapper(StoreMapper.class);

    @Override
    StoreDTO toDTO(Store entity);

    @Override
    Store toEntity(StoreDTO storeDTO);
}