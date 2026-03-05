package de.fhdw.vendix.commons.security.core;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockRequestDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.domain.lock.port.LockCommandPort;
import de.fhdw.vendix.security.api.auth.AppContext;
import de.fhdw.vendix.security.api.auth.AuthContext;
import de.fhdw.vendix.security.api.auth.AuthenticationLifecycleHandler;

import java.time.Clock;
import java.time.Instant;

public class DefaultAuthenticationLifecycleHandler implements AuthenticationLifecycleHandler {

    private final LockCommandPort lockCommandPort;
    private final AppContext appContext;
    private final Clock clock;

    public DefaultAuthenticationLifecycleHandler(LockCommandPort lockCommandPort, AppContext appContext, Clock clock) {
        this.lockCommandPort = lockCommandPort;
        this.appContext = appContext;
        this.clock = clock;
    }

    @Override
    public void onAuthenticationSuccess(AuthContext authContext) {
        LockRequestDTO dto = new LockRequestDTO(
                TargetTypeEnum.ACCOUNT,
                authContext.account().id(),
                appContext.getInstanceUUID(),
                Instant.now(clock),
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