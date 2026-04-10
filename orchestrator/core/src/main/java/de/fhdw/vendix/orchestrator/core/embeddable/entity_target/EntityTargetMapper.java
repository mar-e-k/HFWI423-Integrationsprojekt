package de.fhdw.vendix.orchestrator.core.embeddable.entity_target;

import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface EntityTargetMapper extends EntityMapper<EntityTarget, EntityTargetDTO> {

    @Override
    EntityTargetDTO toDTO(EntityTarget entity);

    @Override
    EntityTarget toEntity(EntityTargetDTO dto);

    @Override
    List<EntityTargetDTO> toDTOs(Iterable<EntityTarget> entities);

    @Override
    List<EntityTarget> toEntities(Iterable<EntityTargetDTO> entityTargetDTOS);
}