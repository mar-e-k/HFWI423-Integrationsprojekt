package de.fhdw.vendix.commons.spring.app.context.register;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.spring.app.context.DomainContextEvent;

public final class RegisterContextInitializedEvent extends DomainContextEvent {

    public RegisterContextInitializedEvent(Object source, RegisterDTO register) {
        super(source, register);
    }
}