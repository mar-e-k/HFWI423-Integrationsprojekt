package de.fhdw.vendix.orchestrator.core.domain.account_role_assignment;

import de.fhdw.vendix.commons.api.domain.account_role_assignment.AccountRoleAssignmentDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import de.fhdw.vendix.orchestrator.core.domain.account.AccountMapper;
import de.fhdw.vendix.orchestrator.core.domain.account_role.AccountRoleMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {
                AccountMapper.class,
                AccountRoleMapper.class
        }
)
public interface AccountRoleAssignmentMapper extends EntityMapper<AccountRoleAssignment, AccountRoleAssignmentDTO> {

    @Override
    AccountRoleAssignmentDTO toDTO(AccountRoleAssignment entity);

    @Override
    AccountRoleAssignment toEntity(AccountRoleAssignmentDTO dto);

    @Override
    List<AccountRoleAssignmentDTO> toDTOs(Iterable<AccountRoleAssignment> entities);

    @Override
    List<AccountRoleAssignment> toEntities(Iterable<AccountRoleAssignmentDTO> accountRoleAssignmentDTOS);
}