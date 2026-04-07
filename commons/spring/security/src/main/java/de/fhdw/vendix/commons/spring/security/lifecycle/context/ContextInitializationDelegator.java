package de.fhdw.vendix.commons.spring.security.lifecycle.context;

import de.fhdw.vendix.commons.spring.security.context.register.RegisterContextInitializedEvent;
import de.fhdw.vendix.commons.spring.security.context.store.StoreContextInitializedEvent;

public interface ContextInitializationDelegator {

    void onRegisterContextInitializedEvent(RegisterContextInitializedEvent event);

    void onStoreContextInitializedEvent(StoreContextInitializedEvent event);
}