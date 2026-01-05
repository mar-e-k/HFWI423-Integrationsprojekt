package de.fhdw.fillialensystem.utility;

import de.fhdw.fillialensystem.persistence.entity.Store;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public final class StoreClient {

    private Store store;
    private int port;

    private final String instanceId = UUID.randomUUID().toString();
    private final String host = "localhost";

    public StoreClient() {}

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @EventListener
    public void onApplicationEvent(WebServerInitializedEvent event) {
        this.port = event.getWebServer().getPort();
    }
}