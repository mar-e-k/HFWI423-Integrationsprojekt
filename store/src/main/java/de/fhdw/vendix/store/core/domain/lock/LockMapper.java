package de.fhdw.vendix.store.core.domain.lock;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LockMapper extends EntityMapper<Lock, LockDTO> {

    LockMapper INSTANCE = Mappers.getMapper(LockMapper.class);

    @Override
    LockDTO toDTO(Lock entity);

    @Override
    Lock toEntity(LockDTO lockDTO);
}