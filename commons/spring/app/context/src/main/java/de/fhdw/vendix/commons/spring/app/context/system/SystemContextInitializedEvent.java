package de.fhdw.vendix.commons.spring.app.context.system;

import org.springframework.context.ApplicationEvent;

public final class SystemContextInitializedEvent extends ApplicationEvent {

    private final SystemContext systemContext;

    public SystemContextInitializedEvent(Object source, SystemContext systemContext) {
        super(source);
        this.systemContext = systemContext;
    }

    public SystemContext getSystemContext() {
        return systemContext;
    }
}
