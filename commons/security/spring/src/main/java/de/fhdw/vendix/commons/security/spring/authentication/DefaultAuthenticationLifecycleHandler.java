package de.fhdw.vendix.commons.security.spring.authentication;

import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.TargetType;
import de.fhdw.vendix.security.api.AuthenticationLifecycleHandler;
import de.fhdw.vendix.security.api.authorization.AuthorizationService;
import de.fhdw.vendix.security.api.context.AppContext;
import de.fhdw.vendix.security.api.context.AuthContext;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
        LockDTO lock = new LockDTO(
                null,
                TargetType.ACCOUNT,
                authContext.accountId(),
                appContext.getInstanceUUID(),
                Instant.now(),
                Instant.now().plus(1, ChronoUnit.HOURS) // TODO: set properties for automatic expiry. Could also be End of Day
        );
        authorizationPort.createLock(lock);
    }

    @Override
    public void onAuthenticationLogout(AuthContext authContext) {
        authorizationPort.deleteLockByTarget(
                TargetType.ACCOUNT,
                authContext.accountId()
        );
    }
}