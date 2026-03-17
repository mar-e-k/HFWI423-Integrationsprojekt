package de.fhdw.vendix.store.core.persistance.account;

import de.fhdw.vendix.commons.spring.core.crud.AbstractEntityCrudAdapter;
import de.fhdw.vendix.store.core.persistance.account_role.AccountRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
class AccountEntityAdapter extends AbstractEntityCrudAdapter<Account, Long> {

    private final AccountRepository accountRepository;

    protected AccountEntityAdapter(AccountRepository accountRepository) {
        super(accountRepository);
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public boolean existsByUuid(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        return accountRepository.existsByUuid(uuid);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        if (username == null) {
            return false;
        }
        return accountRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<Account> findByUuid(UUID uuid) {
        if (uuid == null) {
            return Optional.empty();
        }
        return accountRepository.findByUuid(uuid);
    }

    @Transactional(readOnly = true)
    public Optional<Account> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return accountRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Set<AccountRole> findAllAccountRolesByAccountId(Long id) {
        return accountRepository.findAllAccountRolesByAccountId(id);
    }
}