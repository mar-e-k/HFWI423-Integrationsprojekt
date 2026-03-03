package de.fhdw.vendix.store.core.domain.account_role;

import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountRoleMapper extends EntityMapper<AccountRole, AccountRoleDTO> {

    AccountRoleMapper INSTANCE = Mappers.getMapper(AccountRoleMapper.class);

    @Override
    AccountRoleDTO toDTO(AccountRole entity);

    @Override
    AccountRole toEntity(AccountRoleDTO accountRoleDTO);
}