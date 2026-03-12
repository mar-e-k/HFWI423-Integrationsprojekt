package de.fhdw.vendix.store.core.persistance.lock;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.store.core.persistance.EntityMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = EntityMapperConfig.class)
public interface LockMapper extends EntityMapper<Lock, LockDTO> {

    @Override
    LockDTO toDTO(Lock entity);

    @Override
    Lock toEntity(LockDTO lockDTO);
}