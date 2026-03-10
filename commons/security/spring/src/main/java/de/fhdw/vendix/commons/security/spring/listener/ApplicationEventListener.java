package de.fhdw.vendix.commons.security.spring.listener;

import de.fhdw.vendix.security.api.auth.AppContext;
import de.fhdw.vendix.security.api.auth.AuthenticationLifecycleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

public final class ApplicationEventListener {

    private static final Logger log = LoggerFactory.getLogger(ApplicationEventListener.class);

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