package de.fhdw.vendix.security.api.context;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import org.jspecify.annotations.Nullable;

public interface RegisterContext {
    void setRegister(@Nullable RegisterDTO register) throws ContextAlreadySetException;

    @Nullable RegisterDTO getRegister();
}