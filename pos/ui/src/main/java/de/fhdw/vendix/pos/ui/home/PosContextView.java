package de.fhdw.vendix.pos.ui.home;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.aura.Aura;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.api.domain.connection.ConnectionDTO;
import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.ConnectionProxyService;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.StoreProxyService;
import de.fhdw.vendix.pos.ui.PosAppLayout;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Route(value = "context", layout = PosAppLayout.class)
@RolesAllowed(Role.ROLE_CASHIER)
@StyleSheet(Aura.STYLESHEET)
public class PosContextView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(PosContextView.class);

    private final ConnectionProxyService connectionProxyService;
    private final StoreProxyService storeProxyService;

    private final Set<ConnectionDTO> connections = new HashSet<>();

    public PosContextView(ConnectionProxyService connectionProxyService, StoreProxyService storeProxyService) {
        this.connectionProxyService = connectionProxyService;
        this.storeProxyService = storeProxyService;

        try {
            connections.addAll(Objects.requireNonNull(
                    connectionProxyService.getConnections(
                            null,
                            TargetType.STORE,
                            null,
                            null,
                            null,
                            null
                    ).getBody()));

        } catch (Exception e) {
            log.atWarn().log("No Store Connection");
        }
    }
}