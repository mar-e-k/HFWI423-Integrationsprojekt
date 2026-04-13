package de.fhdw.vendix.orchestrator.core.domain.account_role_assignment;

import de.fhdw.vendix.commons.spring.data.crud.AbstractCrudService;
import org.springframework.stereotype.Service;

@Service
class AccountRoleAssignmentServiceImpl extends AbstractCrudService<AccountRoleAssignment, Long> implements AccountRoleAssignmentService {

    private final AccountRoleAssignmentRepository accountRoleAssignmentRepository;

    AccountRoleAssignmentServiceImpl(AccountRoleAssignmentRepository accountRoleAssignmentRepository) {
        super(accountRoleAssignmentRepository);
        this.accountRoleAssignmentRepository = accountRoleAssignmentRepository;
    }
}