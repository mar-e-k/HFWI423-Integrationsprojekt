package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.LockRequestDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.LockResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LockDTOMapper extends DTOMapper<LockDTO, LockRequestDTO, LockResponseDTO> {

    LockDTOMapper INSTANCE = Mappers.getMapper(LockDTOMapper.class);

    @Override
    LockDTO toDTO(LockRequestDTO dto);

    @Override
    LockResponseDTO toResponseDTO(LockDTO lockDTO);
}