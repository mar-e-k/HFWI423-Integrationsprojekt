package de.fhdw.vendix.pos.core.specification;

import de.fhdw.vendix.commons.api.domain.account.web.AccountQueryApi;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.domain.lock.web.LockCommandApi;
import de.fhdw.vendix.commons.api.domain.lock.web.LockQueryApi;
import de.fhdw.vendix.security.api.authorization.AuthorizationCommandApi;
import de.fhdw.vendix.security.api.authorization.AuthorizationQueryApi;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
class AuthorizationAdapter implements AuthorizationQueryApi, AuthorizationCommandApi {

    private final AccountQueryApi accountQueryApi;
    private final LockQueryApi lockQueryApi;
    private final LockCommandApi lockCommandApi;

    AuthorizationAdapter(AccountQueryApi accountQueryApi, LockQueryApi lockQueryApi, LockCommandApi lockCommandApi) {
        this.accountQueryApi = accountQueryApi;
        this.lockQueryApi = lockQueryApi;
        this.lockCommandApi = lockCommandApi;
    }

    @Override
    public Set<AccountRoleEnum> findRolesByAccountId(Long accountId) {
        return accountQueryApi.findAllRoles(accountId);
    }

    @Override
    public boolean isAccountLocked(Long accountId) {
        return lockQueryApi.existsByTarget(TargetTypeEnum.ACCOUNT, accountId);
    }

    @Override
    public LockDTO createLock(LockDTO lock) {
        return lockCommandApi.create(lock);
    }

    @Override
    public void deleteLockByTarget(TargetTypeEnum targetTypeEnum, long targetId) {
        lockCommandApi.deleteLockByTarget(targetTypeEnum, targetId);
    }

    @Override
    public void deleteAllInstanceLocks(UUID instanceUUID) {
        lockCommandApi.deleteAllInstanceLocks(instanceUUID);
    }

    @Override
    public void deleteAllExpiredLocks() {
        lockCommandApi.deleteAllExpiredLocks();
    }
}