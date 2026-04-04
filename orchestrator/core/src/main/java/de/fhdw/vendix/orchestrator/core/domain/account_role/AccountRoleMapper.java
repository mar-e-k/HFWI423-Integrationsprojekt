package de.fhdw.vendix.orchestrator.core.domain.account_role;

import de.fhdw.vendix.commons.api.domain.account_role.AccountRoleDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface AccountRoleMapper extends EntityMapper<AccountRole, AccountRoleDTO> {

    @Override
    AccountRoleDTO toDTO(AccountRole entity);

    @Override
    AccountRole toEntity(AccountRoleDTO accountRoleDTO);

    @Override
    List<AccountRoleDTO> toDTOs(Iterable<AccountRole> entities);

    @Override
    List<AccountRole> toEntities(Iterable<AccountRoleDTO> accountRoleDTOS);
}