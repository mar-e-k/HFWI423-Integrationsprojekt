package de.fhdw.vendix.commons.spring.core.mapper.bean;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.LockRequestDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.LockResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = SpringMapperConfig.class)
public interface LockDTOMapper extends DTOMapper<LockDTO, LockRequestDTO, LockResponseDTO> {

    @Override
    LockDTO toDomainDTO(LockRequestDTO dto);

    @Override
    LockResponseDTO toResponseDTO(LockDTO lockDTO);
}