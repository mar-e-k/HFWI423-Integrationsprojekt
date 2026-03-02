package de.fhdw.vendix.store.core.domain.role;

import de.fhdw.vendix.commons.api.domain.role.dto.RoleDTO;
import de.fhdw.vendix.commons.api.structure.mapper.GenericEntityMapper;
import org.mapstruct.Mapper;

@Mapper
public interface RoleMapper extends GenericEntityMapper<AccountRole, RoleDTO> {}