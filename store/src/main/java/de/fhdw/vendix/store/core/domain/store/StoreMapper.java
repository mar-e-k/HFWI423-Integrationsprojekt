package de.fhdw.vendix.store.core.domain.store;

import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import org.mapstruct.Mapper;

@Mapper
public interface StoreMapper extends EntityMapper<Store, StoreDTO> {}