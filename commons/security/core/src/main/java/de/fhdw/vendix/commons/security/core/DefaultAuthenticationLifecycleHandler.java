package de.fhdw.vendix.commons.security.core;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.domain.lock.port.LockCommandPort;
import de.fhdw.vendix.security.api.authentication.AppContext;
import de.fhdw.vendix.security.api.authentication.AuthContext;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class DefaultAuthenticationLifecycleHandler implements AuthenticationLifecycleHandler {

    private final AppContext appContext;
    private final LockCommandPort lockCommandPort;

    public DefaultAuthenticationLifecycleHandler(AppContext appContext, LockCommandPort lockCommandPort) {
        this.appContext = appContext;
        this.lockCommandPort = lockCommandPort;
    }

    @Override
    public void onApplicationStart(AppContext appContext) {
        lockCommandPort.deleteAllByExpiresAtNow();
    }

    @Override
    public void onApplicationShutdown(AppContext appContext) {
        lockCommandPort.deleteAllByInstanceUUID(appContext.getInstanceUUID());
    }

    @Override
    public void onAuthenticationSuccess(AuthContext authContext) {
        Long accountID = Objects.requireNonNull(authContext.accountID());
        LockDTO dto = new LockDTO(
                null,
                TargetTypeEnum.ACCOUNT,
                accountID,
                appContext.getInstanceUUID(),
                Instant.now(),
                Instant.now().plus(1, ChronoUnit.HOURS) // TODO: set properties for automatic expiry. Could also be End of Day
        );
        lockCommandPort.create(dto);
    }

    @Override
    public void onAuthenticationLogout(AuthContext authContext) {
        Long accountID = Objects.requireNonNull(authContext.accountID());
        lockCommandPort.deleteByTargetTypeAndTargetId(
                TargetTypeEnum.ACCOUNT,
                accountID
        );
    }
}