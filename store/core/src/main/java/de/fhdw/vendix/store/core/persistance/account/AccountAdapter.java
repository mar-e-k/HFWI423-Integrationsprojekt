package de.fhdw.vendix.store.core.persistance.account;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.port.AccountCommandPort;
import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.spring.core.crud.AbstractDtoCrudAdapter;
import de.fhdw.vendix.store.core.persistance.account_role.AccountRole;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
class AccountAdapter extends AbstractDtoCrudAdapter<Account, AccountDTO, Long> implements AccountQueryPort, AccountCommandPort {

    private final AccountEntityAdapter accountEntityAdapter;
    private final AccountMapper accountMapper;

    AccountAdapter(AccountEntityAdapter accountEntityAdapter, AccountMapper accountMapper) {
        super(accountEntityAdapter, accountMapper);
        this.accountEntityAdapter = accountEntityAdapter;
        this.accountMapper = accountMapper;
    }

    @Override
    public boolean existsByUuid(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        return accountEntityAdapter.existsByUuid(uuid);
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null) {
            return false;
        }
        return accountEntityAdapter.existsByUsername(username);
    }

    @Override
    public Optional<AccountDTO> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return super.findById(id);
    }

    @Override
    public Optional<AccountDTO> findByUUID(UUID uuid) {
        if (uuid == null) {
            return Optional.empty();
        }
        return accountEntityAdapter.findByUuid(uuid).map(accountMapper::toDTO);
    }

    @Override
    public Optional<AccountDTO> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return accountEntityAdapter.findByUsername(username).map(accountMapper::toDTO);
    }

    @Override
    public Set<AccountRoleEnum> findAllAccountRolesByAccountId(Long id) {
        if (id < 0) {
            return Collections.emptySet();
        }
        return accountEntityAdapter.findAllAccountRolesByAccountId(id).stream()
                .map(AccountRole::getRole)
                .collect(Collectors.toUnmodifiableSet());
    }
}