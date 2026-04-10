package de.fhdw.vendix.orchestrator.core.domain.connection;

import de.fhdw.vendix.commons.api.domain.connection.ConnectionDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTargetMapper;
import de.fhdw.vendix.orchestrator.core.embeddable.instance_details.InstanceDetailsMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {
                EntityTargetMapper.class,
                InstanceDetailsMapper.class
        }
)
public interface ConnectionMapper extends EntityMapper<Connection, ConnectionDTO> {

    @Override
    ConnectionDTO toDTO(Connection entity);

    @Override
    Connection toEntity(ConnectionDTO connectionDTO);

    @Override
    List<ConnectionDTO> toDTOs(Iterable<Connection> entities);

    @Override
    List<Connection> toEntities(Iterable<ConnectionDTO> connectionDTOS);
}