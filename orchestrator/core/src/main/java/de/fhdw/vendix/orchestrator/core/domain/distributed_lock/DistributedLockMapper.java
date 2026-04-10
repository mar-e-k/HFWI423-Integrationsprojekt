package de.fhdw.vendix.orchestrator.core.domain.distributed_lock;

import de.fhdw.vendix.commons.api.domain.distributed_lock.DistributedLockDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTargetMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {
                EntityTargetMapper.class,
        }
)
public interface DistributedLockMapper extends EntityMapper<DistributedLock, DistributedLockDTO> {

    @Override
    DistributedLockDTO toDTO(DistributedLock entity);

    @Override
    DistributedLock toEntity(DistributedLockDTO distributedLockDTO);

    @Override
    List<DistributedLockDTO> toDTOs(Iterable<DistributedLock> entities);

    @Override
    List<DistributedLock> toEntities(Iterable<DistributedLockDTO> lockDTOS);
}