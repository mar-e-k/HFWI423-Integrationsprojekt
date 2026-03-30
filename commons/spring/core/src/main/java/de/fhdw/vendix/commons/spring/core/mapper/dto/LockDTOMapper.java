package de.fhdw.vendix.commons.spring.core.mapper.dto;

import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.LockRequestDTO;
import de.fhdw.vendix.commons.api.domain.lock.LockResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface LockDTOMapper extends DTOMapper<LockDTO, LockRequestDTO, LockResponseDTO> {

    @Override
    LockDTO toDomainDTO(LockRequestDTO dto);

    @Override
    LockResponseDTO toResponseDTO(LockDTO lockDTO);

    @Override
    List<LockDTO> toDomainDTOs(Iterable<LockRequestDTO> requestDTOs);

    @Override
    List<LockResponseDTO> toResponseDTOs(Iterable<LockDTO> domainDTOs);
}