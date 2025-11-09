package de.fhdw.kassensystem.persistence.service;

import de.fhdw.kassensystem.persistence.entity.AccountRole;
import de.fhdw.kassensystem.persistence.entity.AccountRoleEnum;
import de.fhdw.kassensystem.persistence.repository.AccountRoleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class AccountRoleService extends CrudService<AccountRole, Long> {

    private final AccountRoleRepository accountRoleRepository;

    public AccountRoleService(AccountRoleRepository accountRoleRepository) {
        super(accountRoleRepository, AccountRole.class);
        this.accountRoleRepository = accountRoleRepository;
    }

    public Optional<AccountRole> findByRole(AccountRoleEnum role) {
        return accountRoleRepository.findByRole(role);
    }
}
