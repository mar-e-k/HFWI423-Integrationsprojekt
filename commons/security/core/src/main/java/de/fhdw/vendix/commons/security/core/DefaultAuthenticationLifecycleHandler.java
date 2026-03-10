package de.fhdw.vendix.commons.security.core;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockRequestDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.domain.lock.port.LockCommandPort;
import de.fhdw.vendix.security.api.auth.AppContext;
import de.fhdw.vendix.security.api.auth.AuthContext;
import de.fhdw.vendix.security.api.auth.AuthenticationLifecycleHandler;

import java.time.Instant;

public class DefaultAuthenticationLifecycleHandler implements AuthenticationLifecycleHandler {

    private final AppContext appContext;
    private final LockCommandPort lockCommandPort;

    public DefaultAuthenticationLifecycleHandler(AppContext appContext, LockCommandPort lockCommandPort) {
        this.appContext = appContext;
        this.lockCommandPort = lockCommandPort;
    }

    @Override
    public void onApplicationStart(AppContext appContext) {
        lockCommandPort.deleteAllExpiredLocks();
    }

    @Override
    public void onApplicationShutdown(AppContext appContext) {
        lockCommandPort.deleteAllByInstanceUUID(appContext.getInstanceUUID());
    }

    @Override
    public void onAuthenticationSuccess(AuthContext authContext) {
        LockRequestDTO dto = new LockRequestDTO(
                TargetTypeEnum.ACCOUNT,
                authContext.account().id(),
                appContext.getInstanceUUID(),
                Instant.now(),
                null // TODO: set properties for automatic expiry
        );
        lockCommandPort.create(dto);
    }

    @Override
    public void onAuthenticationLogout(AuthContext authContext) {
        lockCommandPort.deleteByTargetTypeAndTargetId(
                TargetTypeEnum.ACCOUNT,
                authContext.account().id()
        );
    }
}