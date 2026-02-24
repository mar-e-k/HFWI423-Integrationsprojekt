package de.fhdw.vendix.store.api.mapper;

import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.structure.mapper.GenericEntityMapper;
import de.fhdw.vendix.store.persistence.entity.Store;
import org.mapstruct.Mapper;

@Mapper
public interface StoreMapper extends GenericEntityMapper<Store, StoreDTO> {}