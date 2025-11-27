package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.AccountRole;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.fillialensystem.persistence.repository.AccountRoleRepository;
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
