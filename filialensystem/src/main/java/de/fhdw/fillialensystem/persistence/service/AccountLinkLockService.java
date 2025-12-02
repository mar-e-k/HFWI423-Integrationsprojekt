package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.entity.AccountLinkLock;
import de.fhdw.fillialensystem.persistence.repository.AccountLinkLockRepository;
import de.fhdw.fillialensystem.persistence.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountLinkLockService extends AbstractCrudService<AccountLinkLock, Long> {

    private final AccountRepository accountRepository;

    public AccountLinkLockService(AccountLinkLockRepository accountLinkLockRepository, AccountRepository accountRepository) {
        super(accountLinkLockRepository);
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountLinkLock lockByAccount(Account account) {
        return repository.save(new AccountLinkLock(account));
    }

    @Transactional
    public AccountLinkLock lockByStoreId(Long accountId) {
        Account account = accountRepository.getReferenceById(accountId);
        return lockByAccount(account);
    }

    @Transactional
    public void deleteByAccount(Account account){
        ((AccountLinkLockRepository) repository).deleteByAccount(account);
    }

    @Transactional
    public void deleteByAccountId(Long accountId){
        Account account = accountRepository.getReferenceById(accountId);
        deleteByAccount(account);
    }
}
