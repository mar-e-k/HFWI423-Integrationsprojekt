package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService extends AbstractCrudService<Account, Long> {

    public AccountService(AccountRepository accountRepository) {
        super(accountRepository);
    }

    public Optional<Account> findByUuid(String uuid) {
        return ((AccountRepository) repository).findByUuid(uuid);
    }

    public Optional<Account> findByUsername(String username) {
        return ((AccountRepository) repository).findByUsername(username);
    }
}