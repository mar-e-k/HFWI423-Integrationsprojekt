package de.fhdw.vendix.store.core.persistance.account;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.port.AccountCommandPort;
import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.spring.core.crud.AbstractDtoCrudAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
class AccountAdapter extends AbstractDtoCrudAdapter<Account, AccountDTO, Long> implements AccountQueryPort, AccountCommandPort {

    private static final Logger log = LoggerFactory.getLogger(AccountAdapter.class);
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
    public boolean existsByRole(AccountRoleEnum role) {
        if (role == null) {
            return false;
        }
        return accountEntityAdapter.existsByRole(role);
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
    public Set<AccountRoleEnum> findAllRolesByAccount_Id(Long id) {
        if (id < 0) {
            return Set.of();
        }
        return accountEntityAdapter.findAllAccountRolesByAccountId(id);
    }

    @Override
    public void assignRole(Long accountId, Long roleId) {
        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException();
        }
        if (roleId == null || roleId <= 0) {
            throw new IllegalArgumentException();
        }
        // TODO
    }

    @Override
    public void removeRole(Long accountId, Long roleId) {
        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException();
        }
        if (roleId == null || roleId <= 0) {
            throw new IllegalArgumentException();
        }
        // TODO
    }

    @Override
    public boolean hasRole(Long accountId, Long roleId) {
        return false;
    }
}