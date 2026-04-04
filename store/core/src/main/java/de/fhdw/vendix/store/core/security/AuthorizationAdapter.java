package de.fhdw.vendix.store.core.security;

import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.security.authorization.AuthorizationService;
import de.fhdw.vendix.commons.spring.security.context.app.AppContext;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
class AuthorizationAdapter implements AuthorizationService {

    private final AppContext appContext;

    AuthorizationAdapter(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public LockDTO createLock(LockDTO entity) {
        return new LockDTO(
                null,
                new EntityTargetDTO(
                        0L,
                        TargetType.ACCOUNT
                ),
                appContext.getInstanceUUID(),
                Instant.now(),
                Instant.now()
        );
    }

    @Override
    public void deleteLockByTarget(EntityTargetDTO target) {

    }

    @Override
    public void deleteAllInstanceLocks(UUID instanceUUID) {

    }

    @Override
    public void deleteAllExpiredLocks() {

    }

    @Override
    public Set<Role> findRolesByAccountId(long accountId) {
        return Set.of();
    }

    @Override
    public boolean isAccountLocked(long accountId) {
        return false;
    }
}
