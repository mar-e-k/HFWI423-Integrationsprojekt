package de.fhdw.vendix.store.core.specification;

import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleDTO;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.domain.lock.port.LockCommandPort;
import de.fhdw.vendix.commons.api.domain.lock.port.LockQueryPort;
import de.fhdw.vendix.security.api.authorization.AuthorizationCommandPort;
import de.fhdw.vendix.security.api.authorization.AuthorizationQueryPort;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
class AuthorizationAdapter implements AuthorizationQueryPort, AuthorizationCommandPort {

    private final AccountQueryPort accountQueryPort;
    private final LockQueryPort lockQueryPort;
    private final LockCommandPort lockCommandPort;

    AuthorizationAdapter(AccountQueryPort accountQueryPort, LockQueryPort lockQueryPort, LockCommandPort lockCommandPort) {
        this.accountQueryPort = accountQueryPort;
        this.lockQueryPort = lockQueryPort;
        this.lockCommandPort = lockCommandPort;
    }

    @Override
    public Set<AccountRoleEnum> findRolesByAccountId(Long accountId) {
        return accountQueryPort.findAllRoles(accountId).stream()
                .map(AccountRoleDTO::role)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isAccountLocked(Long accountId) {
        return lockQueryPort.findByTarget(TargetTypeEnum.ACCOUNT, accountId).isPresent();
    }

    @Override
    public LockDTO createLock(LockDTO entity) {
        return lockCommandPort.create(entity);
    }

    @Override
    public void deleteLockByTarget(TargetTypeEnum targetTypeEnum, long targetId) {
        lockCommandPort.deleteLockByTarget(targetTypeEnum, targetId);
    }

    @Override
    public void deleteAllInstanceLocks(UUID instanceUUID) {
        lockCommandPort.deleteAllInstanceLocks(instanceUUID);
    }

    @Override
    public void deleteAllExpiredLocks() {
        lockCommandPort.deleteAllExpiredLocks();
    }
}