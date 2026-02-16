package de.fhdw.vendix.store.persistence.service;

import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import de.fhdw.vendix.store.persistence.entity.AccountRole;
import de.fhdw.vendix.store.persistence.repository.AccountRoleRepository;
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
