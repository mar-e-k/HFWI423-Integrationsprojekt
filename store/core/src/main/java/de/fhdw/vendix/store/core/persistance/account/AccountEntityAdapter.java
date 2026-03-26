package de.fhdw.vendix.store.core.persistance.account;

import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import de.fhdw.vendix.store.core.persistance.account_role.AccountRole;
import de.fhdw.vendix.store.core.persistance.account_role_assignment.AccountRoleAssignment;
import de.fhdw.vendix.store.core.persistance.account_role_assignment.AccountRoleAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
class AccountEntityAdapter extends AbstractEntityCrudAdapter<Account, Long> {

    private final AccountRepository accountRepository;
    private final AccountRoleAssignmentRepository accountRoleAssignmentRepository;

    protected AccountEntityAdapter(AccountRepository accountRepository, AccountRoleAssignmentRepository accountRoleAssignmentRepository) {
        super(accountRepository);
        this.accountRepository = accountRepository;
        this.accountRoleAssignmentRepository = accountRoleAssignmentRepository;
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
    public boolean existsByRole(AccountRoleEnum role) {
        if (role == null) {
            return false;
        }
        return accountRoleAssignmentRepository.existsByRole_Role(role);
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
    public Set<AccountRoleEnum> findAllAccountRolesByAccountId(Long id) {
        if (id == null || id <= 0) {
            return new HashSet<>();
        }
        return accountRoleAssignmentRepository.findAllByAccount_Id(id).stream()
                .map(AccountRoleAssignment::getRole)
                .map(AccountRole::getRole)
                .collect(Collectors.toUnmodifiableSet());
    }
}