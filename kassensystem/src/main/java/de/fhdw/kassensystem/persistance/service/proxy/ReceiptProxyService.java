package de.fhdw.kassensystem.persistance.service.proxy;

import de.fhdw.kassensystem.utility.StoreClient;
import org.springframework.stereotype.Service;

@Service
public class ReceiptProxyService extends AbstractProxyService{

    public ReceiptProxyService(StoreClient storeClient) {
        super(storeClient);
    }


}