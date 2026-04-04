package de.fhdw.vendix.orchestrator.core.domain.lock;

import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTargetMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {
                EntityTargetMapper.class,
        }
)
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