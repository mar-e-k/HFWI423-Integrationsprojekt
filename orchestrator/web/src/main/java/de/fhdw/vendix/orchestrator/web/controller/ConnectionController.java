package de.fhdw.vendix.orchestrator.web.controller;

import de.fhdw.vendix.commons.api.domain.connection.ConnectionDTO;
import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.web.server.orchestrator.api.ConnectionApi;
import de.fhdw.vendix.orchestrator.core.domain.connection.service.ConnectionService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
class ConnectionController implements ConnectionApi {

    private final ConnectionService connectionService;

    ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @Override
    public ResponseEntity<List<ConnectionDTO>> getConnections(
            @Nullable Long targetId,
            @Nullable TargetType targetType,
            @Nullable UUID instanceUUID,
            @Nullable String instanceHost,
            @Nullable String instanceServer,
            @Nullable Integer instancePort
    ) {
        List<ConnectionDTO> filteredConnections = connectionService.findAll().stream()
                .filter(c -> Objects.equals(c.target().id(), targetId))
                .filter(c -> Objects.equals(c.target().type(), targetType))
                .filter(c -> Objects.equals(c.instance().uuid(), instanceUUID))
                .filter(c -> Objects.equals(c.instance().host(), instanceHost))
                .filter(c -> Objects.equals(c.instance().server(), instanceServer))
                .filter(c -> Objects.equals(c.instance().port(), instancePort))
                .toList();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(filteredConnections);
    }

    @Override
    public ResponseEntity<ConnectionDTO> postConnection(ConnectionDTO connectionDTO) {
        ConnectionDTO created = connectionService.create(connectionDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }
}