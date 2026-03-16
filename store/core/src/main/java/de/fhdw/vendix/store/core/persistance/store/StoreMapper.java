package de.fhdw.vendix.store.core.persistance.store;

import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = SpringMapperConfig.class)
public interface StoreMapper extends EntityMapper<Store, StoreDTO> {

    @Override
    StoreDTO toDTO(Store entity);

    @Override
    Store toEntity(StoreDTO storeDTO);
}