package de.fhdw.vendix.commons.spring.app.lifecycle.context;

import de.fhdw.vendix.commons.spring.app.context.register.RegisterContextInitializedEvent;
import de.fhdw.vendix.commons.spring.app.context.store.StoreContextInitializedEvent;

public interface ContextInitializationDelegator {

    void onRegisterContextInitializedEvent(RegisterContextInitializedEvent event);

    void onStoreContextInitializedEvent(StoreContextInitializedEvent event);
}