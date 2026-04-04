package de.fhdw.vendix.commons.spring.security.context.register;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import org.springframework.context.ApplicationEvent;

public final class RegisterContextInitializedEvent extends ApplicationEvent {

    private final RegisterDTO register;

    public RegisterContextInitializedEvent(Object source, RegisterDTO register) {
        super(source);
        this.register = register;
    }

    public RegisterDTO getRegister() {
        return register;
    }
}