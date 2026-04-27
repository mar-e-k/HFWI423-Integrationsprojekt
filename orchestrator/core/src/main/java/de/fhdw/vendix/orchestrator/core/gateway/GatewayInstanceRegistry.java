package de.fhdw.vendix.orchestrator.core.gateway;

import de.fhdw.vendix.orchestrator.core.embeddable.instance_details.InstanceDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GatewayInstanceRegistry {

    private final Map<String, InstanceDetails> activeInstances = new ConcurrentHashMap<>();

    public void register(RegistrationRequest req) {
        activeInstances.put(req.instanceId(), new InstanceDetails(req));
    }

    public List<InstanceDetails> getAll() {
        return List.copyOf(activeInstances.values());
    }
}