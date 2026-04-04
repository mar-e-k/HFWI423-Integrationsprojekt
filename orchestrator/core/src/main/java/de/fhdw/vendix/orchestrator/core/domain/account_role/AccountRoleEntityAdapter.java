package de.fhdw.vendix.orchestrator.core.domain.account_role;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import org.springframework.stereotype.Service;

@Service
class AccountRoleEntityAdapter extends AbstractEntityCrudAdapter<AccountRole, Long> {

    private final AccountRoleRepository accountRoleRepository;

    AccountRoleEntityAdapter(AccountRoleRepository accountRoleRepository) {
        super(accountRoleRepository);
        this.accountRoleRepository = accountRoleRepository;
    }
}
