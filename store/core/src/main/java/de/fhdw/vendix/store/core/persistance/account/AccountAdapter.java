package de.fhdw.vendix.store.core.persistance.account;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.port.AccountCommandPort;
import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.spring.core.crud.AbstractCrudLogAdapter;
import de.fhdw.vendix.store.core.persistance.account_role.AccountRole;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
class AccountAdapter extends AbstractCrudLogAdapter<Account, Long> implements AccountQueryPort, AccountCommandPort {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountAdapter(AccountRepository accountRepository, AccountMapper accountMapper) {
        super(accountRepository);
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    @Override
    public boolean existsByUuid(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("Parameter 'uuid' cannot be null");
        }
        return accountRepository.existsByUuid(uuid);
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'username' cannot be null or empty");
        }
        return accountRepository.existsByUsername(username);
    }

    @Override
    public Optional<AccountDTO> findById(Long id) {
        return super.findById(id)
                .map(accountMapper::toDTO);
    }

    @Override
    public Optional<AccountDTO> findByUUID(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("Parameter 'uuid' cannot be null");
        }
        return accountRepository.findByUuid(uuid)
                .map(accountMapper::toDTO);
    }

    @Override
    public Optional<AccountDTO> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Parameter 'username' cannot be null or blank");
        }
        return accountRepository.findByUsername(username)
                .map(accountMapper::toDTO);
    }

    @Override
    public Set<AccountRoleEnum> findAllAccountRolesByAccountId(Long id) {
        if (id < 0) {
            throw new IllegalArgumentException("Parameter 'id' cannot be negative");
        }
        return accountRepository.findAllAccountRolesByAccountId(id).stream()
                .map(AccountRole::getRole)
                .collect(Collectors.toUnmodifiableSet());
    }
}