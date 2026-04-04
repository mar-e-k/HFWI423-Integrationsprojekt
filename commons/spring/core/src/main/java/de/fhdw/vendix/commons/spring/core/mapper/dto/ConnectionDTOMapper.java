package de.fhdw.vendix.commons.spring.core.mapper.dto;

import de.fhdw.vendix.commons.api.domain.connection.ConnectionDTO;
import de.fhdw.vendix.commons.api.domain.connection.ConnectionRequestDTO;
import de.fhdw.vendix.commons.api.domain.connection.ConnectionResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface ConnectionDTOMapper extends DTOMapper<ConnectionDTO, ConnectionRequestDTO, ConnectionResponseDTO> {

    @Override
    ConnectionDTO toDomainDTO(ConnectionRequestDTO requestDTO);

    @Override
    ConnectionResponseDTO toResponseDTO(ConnectionDTO domainDTO);

    @Override
    List<ConnectionDTO> toDomainDTOs(Iterable<ConnectionRequestDTO> requestDTOs);

    @Override
    List<ConnectionResponseDTO> toResponseDTOs(Iterable<ConnectionDTO> domainDTOs);
}