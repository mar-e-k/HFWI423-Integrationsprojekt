package de.fhdw.vendix.orchestrator.core.domain.account_role_assignment;

import de.fhdw.vendix.commons.api.domain.account_role_assignment.AccountRoleAssignmentDTO;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import de.fhdw.vendix.orchestrator.core.domain.account_role_assignment.service.AccountRoleAssignmentService;
import org.springframework.stereotype.Service;

@Service
class AccountRoleAssignmentAdapter extends AbstractDtoCrudAdapter<AccountRoleAssignment, AccountRoleAssignmentDTO, Long> implements AccountRoleAssignmentService {

    private final AccountRoleAssignmentEntityAdapter accountRoleAssignmentEntityAdapter;
    private final AccountRoleAssignmentMapper accountRoleAssignmentMapper;

    AccountRoleAssignmentAdapter(AccountRoleAssignmentEntityAdapter accountRoleAssignmentEntityAdapter, AccountRoleAssignmentMapper accountRoleAssignmentMapper) {
        super(accountRoleAssignmentEntityAdapter, accountRoleAssignmentMapper);
        this.accountRoleAssignmentEntityAdapter = accountRoleAssignmentEntityAdapter;
        this.accountRoleAssignmentMapper = accountRoleAssignmentMapper;
    }
}