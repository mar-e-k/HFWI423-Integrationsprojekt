package de.fhdw.vendix.commons.spring.security.listener;

import de.fhdw.vendix.commons.spring.security.context.app.AppContext;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

public final class ApplicationEventListener {

    private final AppContext appContext;
    private final AuthenticationLifecycleHandler authenticationLifecycleHandler;

    public ApplicationEventListener(AppContext appContext, AuthenticationLifecycleHandler authenticationLifecycleHandler) {
        this.appContext = appContext;
        this.authenticationLifecycleHandler = authenticationLifecycleHandler;
    }

    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {
        authenticationLifecycleHandler.onApplicationStart(appContext);
    }

    @EventListener
    public void onApplicationShutdownEvent(ContextClosedEvent event) {
        authenticationLifecycleHandler.onApplicationShutdown(appContext);
    }
}