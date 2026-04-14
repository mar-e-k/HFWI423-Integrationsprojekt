package de.fhdw.vendix.pos.web.client.store;

import de.fhdw.vendix.commons.api.domain.connection.ConnectionDTO;
import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.embeddable.InstanceDetailsDTO;
import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.ConnectionProxyService;
import de.fhdw.vendix.commons.spring.web.client.store.api.ArticleProxyService;
import de.fhdw.vendix.commons.spring.web.client.store.api.ReceiptProxyService;
import de.fhdw.vendix.pos.core.register.RegisterContextInitializedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
class StoreClientBindingListener {

    private static final Logger log = LoggerFactory.getLogger(StoreClientBindingListener.class);

    private final ConnectionProxyService connectionProxyService;
    private final StoreHttpServiceProxyFactory factory;
    private final StoreClientRegistry registry;

    public StoreClientBindingListener(
            ConnectionProxyService connectionProxyService,
            StoreHttpServiceProxyFactory factory,
            StoreClientRegistry registry
    ) {
        this.connectionProxyService = connectionProxyService;
        this.factory = factory;
        this.registry = registry;
    }

    @EventListener
    @Order(Ordered.LOWEST_PRECEDENCE)
    public void onRegisterInitialized(RegisterContextInitializedEvent event) {
        if (registry.isInitialized()) {
            log.atWarn().log("Store clients already initialized, skipping binding");
            return;
        }

        Long storeId = event.getRegister().storeId();
        ResponseEntity<ConnectionDTO> response =
                connectionProxyService.getConnectionByTarget(
                        TargetType.STORE,
                        storeId
                );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException(
                    "No connection available for store " + storeId
            );
        }

        InstanceDetailsDTO instance = response.getBody().instance();

        log.atInfo().log("Binding store clients to {}:{}", instance.server(), instance.port());

        ArticleProxyService articleClient = factory.createClient(ArticleProxyService.class, instance);
        ReceiptProxyService receiptClient = factory.createClient(ReceiptProxyService.class, instance);

        registry.initialize(articleClient, receiptClient);

        log.atInfo().log("Store clients initialized successfully");
    }
}