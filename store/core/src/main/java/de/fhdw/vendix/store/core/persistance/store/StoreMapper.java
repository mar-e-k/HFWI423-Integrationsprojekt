package de.fhdw.vendix.store.core.persistance.store;

import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface StoreMapper extends EntityMapper<Long, Store, StoreDTO> {

    @Override
    StoreDTO toDTO(Store entity);

    @Override
    Store toEntity(StoreDTO storeDTO);

    @Override
    List<StoreDTO> toDTOs(Iterable<Store> entities);

    @Override
    List<Store> toEntities(Iterable<StoreDTO> storeDTOS);
}