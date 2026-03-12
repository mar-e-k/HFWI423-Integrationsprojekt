package de.fhdw.vendix.commons.spring.core.mapper.bean;

import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterRequestDTO;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterResponseDTO;
import de.fhdw.vendix.commons.spring.core.mapper.config.DTOMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = DTOMapperConfig.class)
public interface RegisterDTOMapper extends DTOMapper<RegisterDTO, RegisterRequestDTO, RegisterResponseDTO> {

    @Override
    RegisterDTO toDomainDTO(RegisterRequestDTO dto);

    @Override
    RegisterResponseDTO toResponseDTO(RegisterDTO registerDTO);
}