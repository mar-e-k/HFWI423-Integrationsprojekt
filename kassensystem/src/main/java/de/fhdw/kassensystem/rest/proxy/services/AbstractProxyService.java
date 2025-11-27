package de.fhdw.kassensystem.rest.proxy.services;

import de.fhdw.kassensystem.utility.FilialClient;

public abstract class AbstractProxyService {

    protected final FilialClient filialClient;

    public AbstractProxyService(FilialClient filialClient) {
        this.filialClient = filialClient;
    }

    public FilialClient getFilialClient() {
        return filialClient;
    }
}