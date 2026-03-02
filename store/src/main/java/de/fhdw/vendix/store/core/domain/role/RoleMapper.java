package de.fhdw.vendix.store.core.domain.role;

import de.fhdw.vendix.commons.api.domain.role.dto.RoleDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import org.mapstruct.Mapper;

@Mapper
public interface RoleMapper extends EntityMapper<AccountRole, RoleDTO> {}