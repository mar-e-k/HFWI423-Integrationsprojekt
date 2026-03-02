package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.api.domain.role.dto.RoleDTO;
import de.fhdw.vendix.commons.api.domain.role.dto.RoleRequestDTO;
import de.fhdw.vendix.commons.api.domain.role.dto.RoleResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RoleDTOMapper extends DTOMapper<RoleDTO, RoleRequestDTO, RoleResponseDTO> {

    RoleDTOMapper INSTANCE = Mappers.getMapper(RoleDTOMapper.class);

    @Override
    RoleRequestDTO toRequestDTO(RoleDTO roleDTO);

    @Override
    RoleResponseDTO toResponseDTO(RoleDTO roleDTO);
}