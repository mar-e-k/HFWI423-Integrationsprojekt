package de.fhdw.vendix.store.core.specification;

import de.fhdw.vendix.commons.api.domain.account_role.AccountRole;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRoleDTO;
import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.TargetType;
import de.fhdw.vendix.store.core.persistance.lock.port.LockService;
import de.fhdw.vendix.security.api.authorization.AuthorizationService;
import de.fhdw.vendix.store.core.persistance.account.port.AccountService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
class AuthorizationAdapter implements AuthorizationService {

    private final AccountService accountPort;
    private final LockService lockPort;

    AuthorizationAdapter(AccountService accountPort, LockService lockPort) {
        this.accountPort = accountPort;
        this.lockPort = lockPort;
    }

    @Override
    public Set<AccountRole> findRolesByAccountId(Long accountId) {
        return accountPort.findAllRoles(accountId).stream()
                .map(AccountRoleDTO::role)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isAccountLocked(Long accountId) {
        return lockPort.findByTarget(TargetType.ACCOUNT, accountId).isPresent();
    }

    @Override
    public LockDTO createLock(LockDTO entity) {
        return lockPort.create(entity);
    }

    @Override
    public void deleteLockByTarget(TargetType targetType, long targetId) {
        lockPort.deleteLockByTarget(targetType, targetId);
    }

    @Override
    public void deleteAllInstanceLocks(UUID instanceUUID) {
        lockPort.deleteAllInstanceLocks(instanceUUID);
    }

    @Override
    public void deleteAllExpiredLocks() {
        lockPort.deleteAllExpiredLocks();
    }
}