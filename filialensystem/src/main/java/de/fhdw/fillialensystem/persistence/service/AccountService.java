package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService extends AbstractCrudService<Account, Long> {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        super(accountRepository);
        this.accountRepository = accountRepository;
    }

    public Optional<Account> findByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    public Optional<Account> findByUuid(String uuid) {
        return accountRepository.findByUuid(uuid);
    }

    public boolean existsByAccountRole_Role(AccountRoleEnum role) {
        return accountRepository.existsByAccountRole_Role(role);
    }
}