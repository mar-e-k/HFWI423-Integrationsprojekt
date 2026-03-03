package de.fhdw.vendix.store.core.domain.store_system;

import de.fhdw.vendix.commons.api.domain.store_system.dto.StoreSystemDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StoreSystemMapper extends EntityMapper<StoreSystem, StoreSystemDTO> {

    StoreSystemMapper INSTANCE = Mappers.getMapper(StoreSystemMapper.class);

    @Override
    StoreSystemDTO toDTO(StoreSystem entity);

    @Override
    StoreSystem toEntity(StoreSystemDTO storeSystemDTO);
}