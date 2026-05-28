package de.fhdw.vendix.commons.spring.app.bundle.logging;

import ch.qos.logback.classic.LoggerContext;
import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.spring.app.context.DomainContextEvent;
import de.fhdw.vendix.commons.spring.app.context.system.SystemContext;
import de.fhdw.vendix.commons.spring.app.context.system.SystemContextInitializedEvent;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

public final class DefaultAppContextInstantiatorListener implements AppContextInstantiatorListener {

    private final GlobalLabelFilter labelFilter;

    public DefaultAppContextInstantiatorListener() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        labelFilter = new GlobalLabelFilter();
        labelFilter.setContext(loggerContext);
        labelFilter.start();
        loggerContext.addTurboFilter(labelFilter);
    }

    @EventListener
    public void onSystemContextInitializedEvent(SystemContextInitializedEvent event) {
        SystemContext ctx = event.getSystemContext();

        labelFilter.setLabel(ObservabilityLabel.SERVICE_NAME.getLabel(), ctx.getApplicationName());
        labelFilter.setLabel(ObservabilityLabel.HOST.getLabel(), ctx.getHostname());
        labelFilter.setLabel(ObservabilityLabel.INSTANCE.getLabel(), ctx.getInstanceUuid().toString());
    }

    @EventListener
    public void onDomainContextEvent(DomainContextEvent event) {
        Long domainId = switch (event.getDomain()) {
            case StoreDTO s -> s.id();
            case RegisterDTO r -> r.id();
            default -> throw new IllegalStateException("Unknown type: " + event);
        };

        labelFilter.setLabel(ObservabilityLabel.SERVICE_ID.getLabel(), String.valueOf(domainId));
    }
}