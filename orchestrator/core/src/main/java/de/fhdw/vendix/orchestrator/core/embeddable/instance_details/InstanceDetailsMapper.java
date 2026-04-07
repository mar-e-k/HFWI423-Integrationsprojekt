package de.fhdw.vendix.orchestrator.core.embeddable.instance_details;

import de.fhdw.vendix.commons.api.embeddable.InstanceDetailsDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(config = SpringMapperConfig.class)
public interface InstanceDetailsMapper extends EntityMapper<InstanceDetails, InstanceDetailsDTO> {

    @Override
    InstanceDetailsDTO toDTO(InstanceDetails entity);

    @Override
    InstanceDetails toEntity(InstanceDetailsDTO dto);

    @Override
    Set<InstanceDetailsDTO> toDTOs(Iterable<InstanceDetails> entities);

    @Override
    Set<InstanceDetails> toEntities(Iterable<InstanceDetailsDTO> instanceDetailsDTOS);
}