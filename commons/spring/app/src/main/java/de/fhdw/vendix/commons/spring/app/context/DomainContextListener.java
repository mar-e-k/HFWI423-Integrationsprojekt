package de.fhdw.vendix.commons.spring.app.context;

import de.fhdw.vendix.commons.spring.app.context.app.AppContext;
import de.fhdw.vendix.commons.spring.app.context.app.AppContextInitializedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;

public final class DomainContextListener {

    private static final Logger log = LoggerFactory.getLogger(DomainContextListener.class);

    @EventListener
    public void onAppContextInitializedEvent(AppContextInitializedEvent event) {
        AppContext appContext = event.getAppContext();
        MDC.put("__host__", "%s:%d".formatted(appContext.getServerName(), appContext.getServerPort()));
        MDC.put("__instance__", appContext.getInstanceUuid().toString());
    }
}