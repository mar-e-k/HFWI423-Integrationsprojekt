package de.fhdw.kassensystem.persistence.service;

import de.fhdw.kassensystem.persistence.entity.Account;
import de.fhdw.kassensystem.persistence.repository.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountService extends CrudService<Account, Long> {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        super(accountRepository, Account.class);
        this.accountRepository = accountRepository;
    }
}