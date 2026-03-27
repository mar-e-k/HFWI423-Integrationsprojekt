package de.fhdw.vendix.commons.security.spring.authentication;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.security.api.AuthenticationLifecycleHandler;
import de.fhdw.vendix.security.api.authorization.AuthorizationCommandPort;
import de.fhdw.vendix.security.api.context.AppContext;
import de.fhdw.vendix.security.api.context.AuthContext;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class DefaultAuthenticationLifecycleHandler implements AuthenticationLifecycleHandler {

    private final AppContext appContext;
    private final AuthorizationCommandPort authorizationCommandPort;

    public DefaultAuthenticationLifecycleHandler(AppContext appContext, AuthorizationCommandPort authorizationCommandPort) {
        this.appContext = appContext;
        this.authorizationCommandPort = authorizationCommandPort;
    }

    @Override
    public void onApplicationStart(AppContext appContext) {
        authorizationCommandPort.deleteAllExpiredLocks();
    }

    @Override
    public void onApplicationShutdown(AppContext appContext) {
        authorizationCommandPort.deleteAllInstanceLocks(appContext.getInstanceUUID());
    }

    @Override
    public void onAuthenticationSuccess(AuthContext authContext) {
        LockDTO lock = new LockDTO(
                null,
                TargetTypeEnum.ACCOUNT,
                authContext.accountId(),
                appContext.getInstanceUUID(),
                Instant.now(),
                Instant.now().plus(1, ChronoUnit.HOURS) // TODO: set properties for automatic expiry. Could also be End of Day
        );
        authorizationCommandPort.createLock(lock);
    }

    @Override
    public void onAuthenticationLogout(AuthContext authContext) {
        authorizationCommandPort.deleteLockByTarget(
                TargetTypeEnum.ACCOUNT,
                authContext.accountId()
        );
    }
}