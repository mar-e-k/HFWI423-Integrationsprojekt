package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.structure.mapper.GenericDTOMapper;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterRequestDTO;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterResponseDTO;
import org.mapstruct.Mapper;

@Mapper
public interface RegisterDTOMapper extends GenericDTOMapper<RegisterDTO, RegisterRequestDTO, RegisterResponseDTO> {}