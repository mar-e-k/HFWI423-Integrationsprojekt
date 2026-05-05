package de.fhdw.vendix.commons.spring.app.context.app;

import org.springframework.context.ApplicationEvent;

public final class AppContextInitializedEvent extends ApplicationEvent {

    private final AppContext appContext;

    public AppContextInitializedEvent(Object source, AppContext appContext) {
        super(source);
        this.appContext = appContext;
    }

    public AppContext getAppContext() {
        return appContext;
    }
}
