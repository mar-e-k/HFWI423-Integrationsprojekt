package de.fhdw.vendix.commons.spring.app.consul;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import de.fhdw.vendix.commons.spring.app.context.ContextException;
import de.fhdw.vendix.commons.spring.app.context.DomainContextEvent;
import de.fhdw.vendix.commons.spring.app.context.app.AppContext;
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

public final class DefaultConsulInstanceContextListener implements ConsulInstanceContextListener {

    private static final Logger log = LoggerFactory.getLogger(DefaultConsulInstanceContextListener.class);

    private final ConsulServiceRegistry registry;
    private final ConsulDiscoveryProperties properties;
    private final AppContext appContext;

    @Nullable
    private ConsulRegistration registration;

    public DefaultConsulInstanceContextListener(
            ConsulServiceRegistry registry,
            ConsulDiscoveryProperties properties,
            AppContext appContext
    ) {
        this.registry = registry;
        this.properties = properties;
        this.appContext = appContext;
    }

    @EventListener
    public void onApplicationEvent(DomainContextEvent event) {
        ConsulRegistration registration = createRegistration(event.getDomain());
        register(registration);
    }

    private ConsulRegistration createRegistration(DomainDTO domain) {
        NewService newService = new NewService();
        newService.setId(appContext.getInstanceUuid().toString());
        newService.setName(appContext.getApplicationName());
        newService.setAddress(appContext.getHostname());
        newService.setPort(appContext.getServerPort());

        Map<String, String> metadata = new HashMap<>(properties.getMetadata());
        String metadataKey = resolveDomainTypeName(domain);
        String metadataValue = domain.id().toString();
        metadata.put(
                metadataKey,
                metadataValue
        );
        newService.setMeta(metadata);

        NewService.Check check = new NewService.Check();
        check.setHttp(
                String.format("http://%s:%d/actuator/health",
                        appContext.getHostname(),
                        appContext.getServerPort())
        );
        check.setInterval("10s");
        newService.setCheck(check);

        registration = new ConsulRegistration(newService, properties);
        return registration;
    }

    private String resolveDomainTypeName(DomainDTO domain) {
        return switch (domain) {
            case StoreDTO _ -> RoutingHeader.STORE_ROUTING.name();
            case RegisterDTO _ -> RoutingHeader.REGISTER_ROUTING.name();
            default -> throw new ContextException("Invalid domain type: " + domain);
        };
    }

    public void register(ConsulRegistration registration) throws IllegalStateException {
        Assert.notNull(registration, "Parameter 'registration' cannot be null");
        log.atInfo().log("Registering with Consul...");
        if (this.registration != null) {
            throw new IllegalStateException(
                    "Application already specified a registration within Consul. Skipping registration."
            );
        }
        this.registration = registration;
        registry.register(registration);
        log.atInfo().log("Successfully registered with Consul");
    }

    @PreDestroy
    public void deregister() {
        if (registration != null) {
            registry.deregister(registration);
        }
    }
}