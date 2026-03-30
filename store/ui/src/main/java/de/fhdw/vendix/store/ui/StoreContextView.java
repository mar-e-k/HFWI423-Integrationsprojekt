package de.fhdw.vendix.store.ui;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRole;
import de.fhdw.vendix.store.core.persistance.store.port.StoreService;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RolesAllowed(AccountRole.ROLE_ADMIN)
@Route("context")
public class StoreContextView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(StoreContextView.class);
    private final StoreService storePort;

    public StoreContextView(StoreService storePort) {
        this.storePort = storePort;
        log.atInfo().log("Active stores: {}", storePort.getAllActiveStores().toString());
        log.atInfo().log("Inactive stores: {}", storePort.getAllInactiveActiveStores().toString());
        add(new H1("Store Selector"));
    }
}