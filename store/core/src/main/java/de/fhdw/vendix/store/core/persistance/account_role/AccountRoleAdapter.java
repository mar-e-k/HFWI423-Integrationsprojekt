package de.fhdw.vendix.store.core.persistance.account_role;

import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleDTO;
import de.fhdw.vendix.commons.api.domain.account_role.port.AccountRoleCommandPort;
import de.fhdw.vendix.commons.api.domain.account_role.port.AccountRoleQueryPort;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import org.springframework.stereotype.Service;

@Service
class AccountRoleAdapter extends AbstractDtoCrudAdapter<AccountRole, AccountRoleDTO, Long> implements AccountRoleQueryPort, AccountRoleCommandPort {

    private final AccountRoleEntityAdapter accountRoleEntityAdapter;
    private final AccountRoleMapper accountRoleMapper;

    AccountRoleAdapter(AccountRoleEntityAdapter accountRoleEntityAdapter, AccountRoleMapper accountRoleMapper) {
        super(accountRoleEntityAdapter, accountRoleMapper);
        this.accountRoleEntityAdapter = accountRoleEntityAdapter;
        this.accountRoleMapper = accountRoleMapper;
    }
}
