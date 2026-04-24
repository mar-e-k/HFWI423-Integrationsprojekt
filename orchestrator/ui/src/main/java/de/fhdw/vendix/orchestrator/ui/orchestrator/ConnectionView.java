package de.fhdw.vendix.orchestrator.ui.orchestrator;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.spring.security.Role;
import de.fhdw.vendix.orchestrator.core.domain.connection.Connection;
import de.fhdw.vendix.orchestrator.core.domain.connection.ConnectionService;
import de.fhdw.vendix.orchestrator.ui.OrchestratorAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "connection", layout = OrchestratorAppLayout.class)
@RolesAllowed(Role.Constants.ADMIN)
public class ConnectionView extends VerticalLayout {

    private final ConnectionService connectionService;

    public ConnectionView(ConnectionService connectionService) {
        this.connectionService = connectionService;
        add(initGrid());
    }

    private Grid<Connection> initGrid() {
        Grid<Connection> connectionGrid = new Grid<>(Connection.class, false);
        connectionGrid.setHeightFull();
        connectionGrid.setWidthFull();
        connectionGrid.addColumn(Connection::getId)
                .setHeader("Id")
                .setAutoWidth(true)
                .setSortable(true);
        connectionGrid.addColumn(c -> c.getTarget().getId())
                .setHeader("Target Id")
                .setAutoWidth(true)
                .setSortable(true);
        connectionGrid.addColumn(c -> c.getTarget().getType())
                .setHeader("Target Type")
                .setAutoWidth(true)
                .setSortable(true);
        connectionGrid.addColumn(c -> c.getInstance().getUuid())
                .setHeader("Instance UUID")
                .setAutoWidth(true)
                .setSortable(true);
        connectionGrid.addColumn(c -> c.getInstance().getHost())
                .setHeader("Instance Host")
                .setAutoWidth(true)
                .setSortable(true);
        connectionGrid.addColumn(c -> c.getInstance().getServer())
                .setHeader("Instance Server")
                .setAutoWidth(true)
                .setSortable(true);
        connectionGrid.addColumn(c -> c.getInstance().getPort())
                .setHeader("Instance Port")
                .setAutoWidth(true)
                .setSortable(true);
        connectionGrid.addColumn(Connection::getAcquiredAt)
                .setHeader("Acquired At")
                .setAutoWidth(true)
                .setSortable(true);
        connectionGrid.addColumn(Connection::getHeartbeatAt)
                .setHeader("Heartbeat At")
                .setAutoWidth(true)
                .setSortable(true);
        connectionGrid.setItems(connectionService.findAll());
        return connectionGrid;
    }
}