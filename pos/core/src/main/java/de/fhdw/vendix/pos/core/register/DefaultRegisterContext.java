package de.fhdw.vendix.pos.core.register;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.spring.app.lifecycle.app.AppContext;
import de.fhdw.vendix.commons.spring.security.context.ContextAlreadySetException;
import de.fhdw.vendix.commons.spring.security.context.ContextIllegalSourceException;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;

public final class DefaultRegisterContext implements RegisterContext {

    private final AppContext appContext;
    private final ApplicationEventPublisher applicationEventPublisher;

    private @Nullable RegisterDTO register;

    public DefaultRegisterContext(AppContext appContext, ApplicationEventPublisher applicationEventPublisher) {
        this.appContext = appContext;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public synchronized void setRegister(RegisterDTO register) throws ContextAlreadySetException {
        if (register == null) {
            throw new IllegalArgumentException("Parameter 'register' cannot be null");
        }
        if (!appContext.getApplicationName().equalsIgnoreCase("pos")) {
            throw new ContextIllegalSourceException("Register authContext can only be set by a pos application");
        }
        if (this.register != null) {
            throw new ContextAlreadySetException("Register Context was already set and cannot be changed");
        }
        this.register = register;
        applicationEventPublisher.publishEvent(new RegisterContextInitializedEvent(this, register));
    }

    @Override
    public @Nullable RegisterDTO getRegister() {
        return register;
    }
}