package de.fhdw.vendix.store.core.domain.account;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.port.AccountCommandPort;
import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.spring.core.crud.AbstractSpringDataCrudLogAdapter;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
class AccountService extends AbstractSpringDataCrudLogAdapter<Account, Long> implements AccountQueryPort, AccountCommandPort {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper) {
        super(accountRepository);
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    @Override
    public Optional<AccountDTO> findByAccount(AccountDTO accountDTO) {
        if (accountDTO == null) {
            throw new IllegalArgumentException("Parameter 'accountDTO' cannot be null");
        }
        return findByID(accountDTO.id());
    }

    @Override
    public Optional<AccountDTO> findByID(long id) {
        return accountRepository.findById(id)
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
}