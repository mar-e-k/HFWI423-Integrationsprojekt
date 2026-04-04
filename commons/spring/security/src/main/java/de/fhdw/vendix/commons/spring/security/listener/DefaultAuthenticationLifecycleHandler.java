package de.fhdw.vendix.commons.spring.security.listener;

import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.security.authorization.AuthorizationService;
import de.fhdw.vendix.commons.spring.security.context.app.AppContext;
import de.fhdw.vendix.commons.spring.security.context.auth.AuthContext;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class DefaultAuthenticationLifecycleHandler implements AuthenticationLifecycleHandler {

    private final AppContext appContext;
    private final AuthorizationService authorizationPort;

    public DefaultAuthenticationLifecycleHandler(AppContext appContext, AuthorizationService authorizationPort) {
        this.appContext = appContext;
        this.authorizationPort = authorizationPort;
    }

    @Override
    public void onApplicationStart(AppContext appContext) {
        authorizationPort.deleteAllExpiredLocks();
    }

    @Override
    public void onApplicationShutdown(AppContext appContext) {
        authorizationPort.deleteAllInstanceLocks(appContext.getInstanceUUID());
    }

    @Override
    public void onAuthenticationSuccess(AuthContext authContext) {
        EntityTargetDTO entityTarget = new EntityTargetDTO(
                Objects.requireNonNull(authContext.account().id()),
                TargetType.ACCOUNT
        );
        LockDTO lock = new LockDTO(
                null,
                entityTarget,
                appContext.getInstanceUUID(),
                Instant.now(),
                Instant.now().plus(1, ChronoUnit.HOURS) // TODO: set properties for automatic expiry. Could also be End of Day
        );
        authorizationPort.createLock(lock);
    }

    @Override
    public void onAuthenticationLogout(AuthContext authContext) {
        EntityTargetDTO target = new EntityTargetDTO(
                Objects.requireNonNull(authContext.account().id()),
                TargetType.ACCOUNT
        );
        authorizationPort.deleteLockByTarget(target);
    }
}