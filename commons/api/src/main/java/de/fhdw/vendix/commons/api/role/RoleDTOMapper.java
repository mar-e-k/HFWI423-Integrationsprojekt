package de.fhdw.vendix.commons.api.role;

import de.fhdw.vendix.commons.api.marker.mapper.GenericDTOMapper;
import de.fhdw.vendix.commons.api.role.dto.RoleDTO;
import de.fhdw.vendix.commons.api.role.dto.RoleRequestDTO;
import de.fhdw.vendix.commons.api.role.dto.RoleResponseDTO;
import org.mapstruct.Mapper;

@Mapper
public interface RoleDTOMapper extends GenericDTOMapper<RoleDTO, RoleRequestDTO, RoleResponseDTO> {}