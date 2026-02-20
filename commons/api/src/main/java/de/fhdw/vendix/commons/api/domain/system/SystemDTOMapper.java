package de.fhdw.vendix.commons.api.domain.system;

import de.fhdw.vendix.commons.api.domain.system.dto.SystemDTO;
import de.fhdw.vendix.commons.api.domain.system.dto.SystemRequestDTO;
import de.fhdw.vendix.commons.api.domain.system.dto.SystemResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.GenericDTOMapper;
import org.mapstruct.Mapper;

@Mapper
public interface SystemDTOMapper extends GenericDTOMapper<SystemDTO, SystemRequestDTO, SystemResponseDTO> {}