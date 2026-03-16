package de.fhdw.vendix.store.core.persistance.account_role;

import de.fhdw.vendix.commons.api.domain.account_role.port.AccountRoleCommandPort;
import de.fhdw.vendix.commons.api.domain.account_role.port.AccountRoleQueryPort;
import de.fhdw.vendix.commons.spring.core.crud.AbstractCrudLogAdapter;
import org.springframework.stereotype.Service;

@Service
class AccountRoleAdapter extends AbstractCrudLogAdapter<AccountRole, Long> implements AccountRoleQueryPort, AccountRoleCommandPort {

    private final AccountRoleRepository accountRoleRepository;
    private final AccountRoleMapper accountRoleMapper;

    public AccountRoleAdapter(AccountRoleRepository accountRoleRepository, AccountRoleMapper accountRoleMapper) {
        super(accountRoleRepository);
        this.accountRoleRepository = accountRoleRepository;
        this.accountRoleMapper = accountRoleMapper;
    }
}
