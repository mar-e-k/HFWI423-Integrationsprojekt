package de.fhdw.vendix.orchestrator.core.domain.account;

import de.fhdw.vendix.commons.spring.data.crud.AbstractCrudService;
import de.fhdw.vendix.orchestrator.core.domain.account_role.AccountRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
class AccountServiceImpl extends AbstractCrudService<Account, Long> implements AccountService {

    private final AccountRepository accountRepository;

    protected AccountServiceImpl(AccountRepository accountRepository) {
        super(accountRepository);
        this.accountRepository = accountRepository;
    }

    @Override
    public boolean existsByUuid(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        return accountRepository.existsByUuid(uuid);
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return accountRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return accountRepository.existsByPhone(phone);
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return accountRepository.existsByEmail(email);
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