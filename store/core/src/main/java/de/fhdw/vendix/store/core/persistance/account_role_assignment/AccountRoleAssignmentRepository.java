package de.fhdw.vendix.store.core.persistance.account_role_assignment;

import de.fhdw.vendix.commons.api.domain.account_role.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

interface AccountRoleAssignmentRepository extends JpaRepository<AccountRoleAssignment, Long> {

    Set<AccountRoleAssignment> findAllByAccount_Id(Long accountId);

    Set<AccountRoleAssignment> findAllByRole_Id(Long roleId);

    Set<AccountRoleAssignment> findAllByRole_Role(AccountRole roleRole);
}