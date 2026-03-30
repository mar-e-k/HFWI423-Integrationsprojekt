package de.fhdw.vendix.pos.core.specification;

import de.fhdw.vendix.commons.api.domain.account_role.AccountRole;
import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.TargetType;
import de.fhdw.vendix.security.api.authorization.AuthorizationService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
class AuthorizationAdapter implements AuthorizationService {

    AuthorizationAdapter() {
    }

    @Override
    public LockDTO createLock(LockDTO entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteLockByTarget(TargetType targetType, long targetId) {

    }

    @Override
    public void deleteAllInstanceLocks(UUID instanceUUID) {

    }

    @Override
    public void deleteAllExpiredLocks() {

    }

    @Override
    public Set<AccountRole> findRolesByAccountId(Long accountId) {
        return Set.of();
    }

    @Override
    public boolean isAccountLocked(Long accountId) {
        return false;
    }
}