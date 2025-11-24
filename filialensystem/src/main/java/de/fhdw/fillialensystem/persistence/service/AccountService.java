package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService extends CrudService<Account, Long> {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        super(accountRepository, Account.class);
        this.accountRepository = accountRepository;
    }

    public Optional<Account> findByAccountId(Integer accountId) {
        return accountRepository.findByAccountId(accountId);
    }
}