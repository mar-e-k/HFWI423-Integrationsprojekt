package de.fhdw.vendix.commons.spring.app.consul;

import de.fhdw.vendix.commons.spring.app.context.app.AppContext;
import de.fhdw.vendix.commons.spring.web.api.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.model.http.agent.NewService;
import org.springframework.cloud.consul.model.http.agent.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ConsulClientUpdater {

    private static final Logger log = LoggerFactory.getLogger(ConsulClientUpdater.class);

    private final AppContext appContext;
    private final ConsulClient consulClient;

    public ConsulClientUpdater(AppContext appContext, ConsulClient consulClient) {
        this.appContext = appContext;
        this.consulClient = consulClient;
    }

    public void updateMetadataTag(String key, String value) {
        try {
            log.atInfo().log("Updating Consul tags...");
            Map<String, Service> agentServicesResponse = ResponseUtils.extractMap(consulClient.getAgentServices());
            Service service = agentServicesResponse.get(appContext.getInstanceUuid().toString());

            if (service != null) {
                List<String> updatedTags = new ArrayList<>(service.getTags());

                String targetTag = key + "=" + value;
                updatedTags.removeIf(tag -> tag.startsWith(key + "="));
                updatedTags.add(targetTag);

                NewService newService = new NewService();
                newService.setId(service.getId());
                newService.setName(service.getService());
                newService.setAddress(service.getAddress());
                newService.setPort(service.getPort());
                newService.setTags(updatedTags);

                // TODO
                consulClient.agentServiceRegister("", newService);
                log.atInfo().log("Successfully updated Consul tags: {}", targetTag);
            }
        } catch (Exception e) {
            log.atError().log("Failed to update Consul metadata", e);
        }
    }
}