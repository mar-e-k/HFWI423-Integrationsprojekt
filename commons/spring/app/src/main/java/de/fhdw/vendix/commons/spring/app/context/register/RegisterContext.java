package de.fhdw.vendix.commons.spring.app.context.register;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.spring.security.context.ContextAlreadySetException;
import org.jspecify.annotations.Nullable;

public interface RegisterContext {
    void setRegister(RegisterDTO register) throws ContextAlreadySetException;

    @Nullable RegisterDTO getRegister();
}