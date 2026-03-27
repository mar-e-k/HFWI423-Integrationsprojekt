package de.fhdw.vendix.store.core.persistance.account;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.port.AccountCommandPort;
import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleDTO;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import de.fhdw.vendix.store.core.persistance.account_role.AccountRoleMapper;
import de.fhdw.vendix.store.core.persistance.account_role_assignment.AccountRoleAssignment;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
class AccountAdapter extends AbstractDtoCrudAdapter<Account, AccountDTO, Long> implements AccountQueryPort, AccountCommandPort {

    private final AccountEntityAdapter accountEntityAdapter;
    private final AccountMapper accountMapper;
    private final AccountRoleMapper accountRoleMapper;

    AccountAdapter(AccountEntityAdapter accountEntityAdapter, AccountMapper accountMapper, AccountRoleMapper accountRoleMapper) {
        super(accountEntityAdapter, accountMapper);
        this.accountEntityAdapter = accountEntityAdapter;
        this.accountMapper = accountMapper;
        this.accountRoleMapper = accountRoleMapper;
    }

    @Override
    public Optional<AccountDTO> findByUUID(UUID uuid) {
        if (uuid == null) {
            return Optional.empty();
        }
        return accountEntityAdapter.findByUuid(uuid).map(accountMapper::toDTO);
    }

    // TODO
    @Override
    public Optional<AccountDTO> findByPhone(String phone) {
        return Optional.empty();
    }

    // TODO
    @Override
    public Optional<AccountDTO> findByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public Optional<AccountDTO> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return accountEntityAdapter.findByUsername(username).map(accountMapper::toDTO);
    }

    @Override
    public Set<AccountRoleDTO> findAllRoles(Long id) {
        if (id < 0) {
            return Set.of();
        }
        return accountEntityAdapter.findAllRoles(id).stream()
                .map(accountRoleMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
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
}