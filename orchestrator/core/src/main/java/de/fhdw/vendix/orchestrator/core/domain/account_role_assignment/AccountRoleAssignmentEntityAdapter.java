package de.fhdw.vendix.orchestrator.core.domain.account_role_assignment;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import org.springframework.stereotype.Service;

@Service
class AccountRoleAssignmentEntityAdapter extends AbstractEntityCrudAdapter<AccountRoleAssignment, Long> {

    private final AccountRoleAssignmentRepository accountRoleAssignmentRepository;

    AccountRoleAssignmentEntityAdapter(AccountRoleAssignmentRepository accountRoleAssignmentRepository) {
        super(accountRoleAssignmentRepository);
        this.accountRoleAssignmentRepository = accountRoleAssignmentRepository;
    }
}