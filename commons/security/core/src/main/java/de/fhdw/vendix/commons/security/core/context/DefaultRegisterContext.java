package de.fhdw.vendix.commons.security.core.context;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.security.api.context.ContextAlreadySetException;
import de.fhdw.vendix.security.api.context.RegisterContext;
import org.jspecify.annotations.Nullable;

public class DefaultRegisterContext implements RegisterContext {

    private @Nullable RegisterDTO register;

    @Override
    public void setRegister(@Nullable RegisterDTO register) throws ContextAlreadySetException {
        if (this.register == null) {
            this.register = register;
        } else {
            throw new ContextAlreadySetException("Register Context was already set");
        }
    }

    @Override
    public @Nullable RegisterDTO getRegister() {
        return register;
    }
}