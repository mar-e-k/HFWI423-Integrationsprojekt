package de.fhdw.vendix.pos.core.register;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.spring.app.context.ContextAlreadySetException;
import de.fhdw.vendix.commons.spring.app.context.ContextException;
import de.fhdw.vendix.commons.spring.app.context.ContextIllegalSourceException;
import de.fhdw.vendix.commons.spring.app.context.app.AppContext;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DefaultRegisterContext implements RegisterContext {

    private final AppContext appContext;
    private final ApplicationEventPublisher applicationEventPublisher;

    private @Nullable RegisterDTO register;

    public DefaultRegisterContext(AppContext appContext, ApplicationEventPublisher applicationEventPublisher) {
        this.appContext = appContext;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public synchronized void setRegister(RegisterDTO register) throws ContextException {
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