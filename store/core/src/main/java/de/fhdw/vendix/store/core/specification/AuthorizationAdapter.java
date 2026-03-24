package de.fhdw.vendix.store.core.specification;

import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.domain.lock.port.LockCommandPort;
import de.fhdw.vendix.commons.api.domain.lock.port.LockQueryPort;
import de.fhdw.vendix.security.api.authorization.AuthorizationCommandApi;
import de.fhdw.vendix.security.api.authorization.AuthorizationQueryApi;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
class AuthorizationAdapter implements AuthorizationQueryApi, AuthorizationCommandApi {

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
        return accountQueryPort.findAllRoles(accountId);
    }

    @Override
    public boolean isAccountLocked(Long accountId) {
        return lockQueryPort.existsByTarget(TargetTypeEnum.ACCOUNT, accountId);
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