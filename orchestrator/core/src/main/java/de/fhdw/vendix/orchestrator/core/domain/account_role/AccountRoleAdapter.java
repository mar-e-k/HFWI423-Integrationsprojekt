package de.fhdw.vendix.orchestrator.core.domain.account_role;

import de.fhdw.vendix.commons.api.domain.account_role.AccountRoleDTO;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import de.fhdw.vendix.orchestrator.core.domain.account_role.service.AccountRoleService;
import org.springframework.stereotype.Service;

@Service
class AccountRoleAdapter extends AbstractDtoCrudAdapter<AccountRole, AccountRoleDTO, Long> implements AccountRoleService {

    private final AccountRoleEntityAdapter accountRoleEntityAdapter;
    private final AccountRoleMapper accountRoleMapper;

    AccountRoleAdapter(AccountRoleEntityAdapter accountRoleEntityAdapter, AccountRoleMapper accountRoleMapper) {
        super(accountRoleEntityAdapter, accountRoleMapper);
        this.accountRoleEntityAdapter = accountRoleEntityAdapter;
        this.accountRoleMapper = accountRoleMapper;
    }
}
