package de.fhdw.kassensystem.persistance.service.proxy;

import de.fhdw.kassensystem.utility.StoreClient;

public abstract class AbstractProxyService {

    protected final StoreClient storeClient;

    public AbstractProxyService(StoreClient storeClient) {
        this.storeClient = storeClient;
    }

    public StoreClient getFilialClient() {
        return storeClient;
    }
}