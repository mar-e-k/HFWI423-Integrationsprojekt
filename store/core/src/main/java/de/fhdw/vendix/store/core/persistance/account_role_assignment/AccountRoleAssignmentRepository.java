package de.fhdw.vendix.store.core.persistance.account_role_assignment;

import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AccountRoleAssignmentRepository extends JpaRepository<AccountRoleAssignment, Long> {

    boolean existsByRole_Role(AccountRoleEnum role);

    Set<AccountRoleAssignment> findAllByAccount_Id(Long id);
}