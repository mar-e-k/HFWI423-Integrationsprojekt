package de.fhdw.vendix.pos.web.client.store;

import de.fhdw.vendix.commons.spring.web.client.store.api.ArticleProxyService;
import de.fhdw.vendix.commons.spring.web.client.store.api.ReceiptProxyService;
import org.springframework.stereotype.Component;

@Component
public class StoreClients {
    
    private final StoreClientRegistry registry;

    public StoreClients(StoreClientRegistry registry) {
        this.registry = registry;
    }

    public ArticleProxyService article() {
        return registry.getArticle();
    }

    public ReceiptProxyService receipt() {
        return registry.getReceipt();
    }
}