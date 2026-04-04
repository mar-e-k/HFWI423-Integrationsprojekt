package de.fhdw.vendix.commons.spring.security.context.register;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.spring.security.context.ContextAlreadySetException;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;

public final class DefaultRegisterContext implements RegisterContext {

    private final ApplicationEventPublisher applicationEventPublisher;

    private @Nullable RegisterDTO register;

    public DefaultRegisterContext(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public synchronized void setRegister(RegisterDTO register) throws ContextAlreadySetException {
        if (register == null) {
            throw new IllegalArgumentException("Parameter 'register' cannot be null");
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