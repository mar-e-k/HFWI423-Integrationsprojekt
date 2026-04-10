package de.fhdw.vendix.orchestrator.core.domain.account;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import de.fhdw.vendix.orchestrator.core.domain.account_role.AccountRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
class AccountServiceImpl extends AbstractEntityCrudAdapter<Account, Long> implements AccountService {

    private final AccountRepository accountRepository;

    protected AccountServiceImpl(AccountRepository accountRepository) {
        super(accountRepository);
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findByUuid(UUID uuid) {
        if (uuid == null) {
            return Optional.empty();
        }
        return accountRepository.findByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return accountRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findByPhone(String phone) {
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findByEmail(String email) {
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountRole> findAllRolesById(Long id) {
        if (id == null || id < 1) {
            return List.of();
        }
        return accountRepository.findAllRolesByAccountId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountRole> findAllRolesByUuid(UUID uuid) {
        if (uuid == null) {
            return List.of();
        }
        return accountRepository.findAllRolesByAccountUuid(uuid);
    }
}