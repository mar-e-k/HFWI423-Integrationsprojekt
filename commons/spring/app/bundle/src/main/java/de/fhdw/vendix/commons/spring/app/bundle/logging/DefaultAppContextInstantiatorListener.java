package de.fhdw.vendix.commons.spring.app.bundle.logging;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.spring.app.context.DomainContextEvent;
import de.fhdw.vendix.commons.spring.app.context.LogbackAttribute;
import de.fhdw.vendix.commons.spring.app.context.system.SystemContext;
import de.fhdw.vendix.commons.spring.app.context.system.SystemContextInitializedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.util.Assert;

public final class DefaultAppContextInstantiatorListener implements AppContextInstantiatorListener {

    private static final Logger log = LoggerFactory.getLogger(DefaultAppContextInstantiatorListener.class);

    @EventListener
    public void onSystemContextInitializedEvent(SystemContextInitializedEvent event) {
        log.atInfo().log("Adding Application Context Information...");
        SystemContext context = event.getSystemContext();
        MDC.put(LogbackAttribute.SERVICE_NAME.getKeyName(), context.getApplicationName());
        MDC.put(LogbackAttribute.HOST.getKeyName(), context.getHostname());
        MDC.put(LogbackAttribute.INSTANCE.getKeyName(), context.getInstanceUuid().toString());
        MDC.put(LogbackAttribute.DOMAIN_TYPE.getKeyName(), context.getApplicationName());
        MDC.put(LogbackAttribute.DOMAIN_ID.getKeyName(), "Not specified");
        log.atInfo().log("Successfully added Application Context Information");
    }

    @EventListener
    public void onDomainContextEvent(DomainContextEvent event) {
        log.atInfo().log("Adding Domain Context Information...");
        Long domainId = switch (event.getDomain()) {
            case StoreDTO s -> s.id();
            case RegisterDTO r -> r.id();
            default -> throw new IllegalStateException("Unknown domain type: '%s'".formatted(event));
        };
        Assert.notNull(domainId, "Variable 'domainId' must not be null");
        MDC.put(LogbackAttribute.DOMAIN_ID.getKeyName(), String.valueOf(domainId));
        log.atInfo().log("Successfully added Domain Context Information");
    }
}