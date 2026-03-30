package de.fhdw.vendix.store.core.persistance.lock;

import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface LockMapper extends EntityMapper<Lock, LockDTO> {

    @Override
    LockDTO toDTO(Lock entity);

    @Override
    Lock toEntity(LockDTO lockDTO);

    @Override
    List<LockDTO> toDTOs(Iterable<Lock> entities);

    @Override
    List<Lock> toEntities(Iterable<LockDTO> lockDTOS);
}