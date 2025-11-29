package de.fhdw.fillialensystem.utility;

import de.fhdw.fillialensystem.persistence.entity.Store;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;


@Component
public class StoreClient implements ApplicationListener<WebServerInitializedEvent> {

    private Store store;

    private String host;

    private int port;

    public StoreClient() {
        super();
        host = "localhost";
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        this.port = event.getWebServer().getPort();
    }
}