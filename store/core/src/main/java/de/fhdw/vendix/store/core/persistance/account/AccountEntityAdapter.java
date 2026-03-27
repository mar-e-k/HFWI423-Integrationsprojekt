package de.fhdw.vendix.store.core.persistance.account;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
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

    Set<AccountRole> findAllRoles(Long id) {
        return accountRepository.findAllRoles(id);
    }
}