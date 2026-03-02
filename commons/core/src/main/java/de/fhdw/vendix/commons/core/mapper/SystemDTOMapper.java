package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.domain.system.dto.SystemDTO;
import de.fhdw.vendix.commons.api.domain.system.dto.SystemRequestDTO;
import de.fhdw.vendix.commons.api.domain.system.dto.SystemResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SystemDTOMapper extends DTOMapper<SystemDTO, SystemRequestDTO, SystemResponseDTO> {

    SystemDTOMapper INSTANCE = Mappers.getMapper(SystemDTOMapper.class);

    @Override
    SystemRequestDTO toRequestDTO(SystemDTO systemDTO);

    @Override
    SystemResponseDTO toResponseDTO(SystemDTO systemDTO);
}