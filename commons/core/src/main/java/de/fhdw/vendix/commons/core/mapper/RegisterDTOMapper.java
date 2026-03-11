package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterRequestDTO;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RegisterDTOMapper extends DTOMapper<RegisterDTO, RegisterRequestDTO, RegisterResponseDTO> {

    RegisterDTOMapper INSTANCE = Mappers.getMapper(RegisterDTOMapper.class);

    @Override
    RegisterDTO toDTO(RegisterRequestDTO dto);

    @Override
    RegisterResponseDTO toResponseDTO(RegisterDTO registerDTO);
}