package de.fhdw.vendix.orchestrator.core.domain.lock;

import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.LockRequestDTO;
import de.fhdw.vendix.commons.api.domain.lock.LockResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(config = SpringMapperConfig.class)
public interface LockDTOMapper extends DTOMapper<LockDTO, LockRequestDTO, LockResponseDTO> {

    @Override
    LockDTO toDomainDTO(LockRequestDTO dto);

    @Override
    LockResponseDTO toResponseDTO(LockDTO lockDTO);

    @Override
    Set<LockDTO> toDomainDTOs(Iterable<LockRequestDTO> requestDTOs);

    @Override
    Set<LockResponseDTO> toResponseDTOs(Iterable<LockDTO> domainDTOs);
}