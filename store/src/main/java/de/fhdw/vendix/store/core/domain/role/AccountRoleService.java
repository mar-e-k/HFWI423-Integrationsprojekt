package de.fhdw.vendix.store.core.domain.role;

import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import de.fhdw.vendix.store.core.domain.AbstractCrudService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountRoleService extends AbstractCrudService<AccountRole, Long> {

    public AccountRoleService(AccountRoleRepository accountRoleRepository) {
        super(accountRoleRepository);
    }

    public Optional<AccountRole> findByRole(AccountRoleEnum role) {
        return ((AccountRoleRepository) repository).findByRole(role);
    }
}
