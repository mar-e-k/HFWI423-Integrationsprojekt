package de.fhdw.vendix.orchestrator.core.domain.account;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRoleDTO;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import de.fhdw.vendix.orchestrator.core.domain.account_role.AccountRoleMapper;
import de.fhdw.vendix.orchestrator.core.domain.account.service.AccountService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
class AccountAdapter extends AbstractDtoCrudAdapter<Account, AccountDTO, Long> implements AccountService {

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

    @Override
    public Optional<AccountDTO> findByPhone(String phone) {
        return Optional.empty();
    }

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
    public void assignRole(long accountId, long roleId) {
        if (accountId <= 0) {
            throw new IllegalArgumentException();
        }
        if (roleId <= 0) {
            throw new IllegalArgumentException();
        }
        // TODO
    }

    @Override
    public void removeRole(long accountId, long roleId) {
        if (accountId <= 0) {
            throw new IllegalArgumentException();
        }
        if (roleId <= 0) {
            throw new IllegalArgumentException();
        }
        // TODO
    }
}