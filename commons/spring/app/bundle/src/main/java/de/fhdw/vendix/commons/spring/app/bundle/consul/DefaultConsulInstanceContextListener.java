package de.fhdw.vendix.commons.spring.app.bundle.consul;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import de.fhdw.vendix.commons.spring.app.context.ContextException;
import de.fhdw.vendix.commons.spring.app.context.DomainContextEvent;
import de.fhdw.vendix.commons.spring.app.context.system.SystemContext;
import de.fhdw.vendix.commons.spring.web.core.RoutingHeader;
import jakarta.annotation.Nullable;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.model.http.agent.NewService;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.context.event.EventListener;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public final class DefaultConsulInstanceContextListener implements ConsulInstanceContextListener {

    private static final Logger log = LoggerFactory.getLogger(DefaultConsulInstanceContextListener.class);

    private final ConsulServiceRegistry registry;
    private final ConsulDiscoveryProperties properties;
    private final SystemContext systemContext;

    private final AtomicReference<String> currentActiveServiceId = new AtomicReference<>();

    @Nullable
    private volatile ConsulRegistration registration;

    public DefaultConsulInstanceContextListener(
            ConsulServiceRegistry registry,
            ConsulDiscoveryProperties properties,
            ConsulRegistration registration,
            SystemContext systemContext
    ) {
        this.registry = registry;
        this.properties = properties;
        this.systemContext = systemContext;
        this.currentActiveServiceId.set(registration.getInstanceId());
    }

    @EventListener
    public void onApplicationEvent(DomainContextEvent event) {
        ConsulRegistration registration = createRegistration(event.getDomain());
        register(registration);
    }

    private ConsulRegistration createRegistration(DomainDTO domain) {
        NewService newService = new NewService();

        String targetServiceId = systemContext.getApplicationName() + "-" + domain.id().toString();
        newService.setId(targetServiceId);
        newService.setName(systemContext.getApplicationName());

        String resolvedHost = properties.getHostname();
        newService.setAddress(resolvedHost);
        newService.setPort(systemContext.getServerPort());

        Map<String, String> metadata = new HashMap<>(properties.getMetadata());
        String metadataKey = resolveDomainTypeName(domain);
        String metadataValue = domain.id().toString();
        metadata.put(metadataKey, metadataValue);
        newService.setMeta(metadata);

        NewService.Check check = new NewService.Check();
        check.setHttp(String.format("http://%s:%d/actuator/health", resolvedHost, systemContext.getServerPort()));
        check.setInterval("10s");
        newService.setCheck(check);

        return new ConsulRegistration(newService, properties);
    }

    private String resolveDomainTypeName(DomainDTO domain) {
        return switch (domain) {
            case StoreDTO _ -> RoutingHeader.STORE_ROUTING.getHeader();
            case RegisterDTO _ -> RoutingHeader.REGISTER_ROUTING.getHeader();
            default -> throw new ContextException("Invalid domain type: " + domain);
        };
    }

    public synchronized void register(ConsulRegistration newRegistration) {
        Assert.notNull(newRegistration, "Parameter 'newRegistration' cannot be null");
        String newServiceId = newRegistration.getInstanceId();

        String oldServiceId = currentActiveServiceId.getAndSet(newServiceId);

        if (oldServiceId != null && !oldServiceId.equals(newServiceId)) {
            log.atInfo().log("Context shifted. Proactively deregistering previous ID [{}]...", oldServiceId);
            deregisterServiceId(oldServiceId);
        }

        this.registration = newRegistration;

        log.atInfo().log("Synchronizing instance [{}] with Consul...", newServiceId);
        registry.register(newRegistration);
        log.atInfo().log("Successfully synchronized registration state.");
    }

    private void deregisterServiceId(String serviceId) {
        try {
            NewService dummyService = new NewService();
            dummyService.setId(serviceId);
            ConsulRegistration dummyRegistration = new ConsulRegistration(dummyService, properties);
            registry.deregister(dummyRegistration);
        } catch (Exception e) {
            log.atWarn().log("Could not cleanly clear out old ID [{}]: {}", serviceId, e.getMessage());
        }
    }

    @PreDestroy
    public void deregister() {
        if (registration != null) {
            registry.deregister(registration);
        }
    }
}