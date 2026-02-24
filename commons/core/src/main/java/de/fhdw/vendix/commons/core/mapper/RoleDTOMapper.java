package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.structure.mapper.GenericDTOMapper;
import de.fhdw.vendix.commons.api.domain.role.dto.RoleDTO;
import de.fhdw.vendix.commons.api.domain.role.dto.RoleRequestDTO;
import de.fhdw.vendix.commons.api.domain.role.dto.RoleResponseDTO;
import org.mapstruct.Mapper;

@Mapper
public interface RoleDTOMapper extends GenericDTOMapper<RoleDTO, RoleRequestDTO, RoleResponseDTO> {}