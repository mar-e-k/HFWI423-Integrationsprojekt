package de.fhdw.vendix.pos.persistance.service.proxy;

import de.fhdw.vendix.pos.utility.StoreClient;
import org.springframework.web.reactive.function.client.WebClient;

public abstract class AbstractProxyService {

    private final StoreClient storeClient;

    public AbstractProxyService(StoreClient storeClient) {
        this.storeClient = storeClient;
    }

    public WebClient getWebClient() {
        return storeClient.getWebClient();
    }
}