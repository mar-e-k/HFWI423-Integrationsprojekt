package de.fhdw.vendix.orchestrator.core.domain.account_role;

import de.fhdw.vendix.commons.spring.data.crud.AbstractCrudService;
import org.springframework.stereotype.Service;

@Service
class AccountRoleServiceImpl extends AbstractCrudService<AccountRole, Long> implements AccountRoleService {

    private final AccountRoleRepository accountRoleRepository;

    AccountRoleServiceImpl(AccountRoleRepository accountRoleRepository) {
        super(accountRoleRepository);
        this.accountRoleRepository = accountRoleRepository;
    }
}