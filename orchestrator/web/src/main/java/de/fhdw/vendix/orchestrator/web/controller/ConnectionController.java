package de.fhdw.vendix.orchestrator.web.controller;

import de.fhdw.vendix.commons.api.domain.connection.ConnectionDTO;
import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.web.server.orchestrator.api.ConnectionApi;
import de.fhdw.vendix.orchestrator.core.domain.connection.Connection;
import de.fhdw.vendix.orchestrator.core.domain.connection.ConnectionMapper;
import de.fhdw.vendix.orchestrator.core.domain.connection.ConnectionService;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTarget;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
class ConnectionController implements ConnectionApi {

    private final ConnectionService connectionService;
    private final ConnectionMapper connectionMapper;

    ConnectionController(ConnectionService connectionService, ConnectionMapper connectionMapper) {
        this.connectionService = connectionService;
        this.connectionMapper = connectionMapper;
    }

    @Override
    public ResponseEntity<Set<ConnectionDTO>> getConnections(
            @Nullable Long targetId,
            @Nullable TargetType targetType,
            @Nullable UUID instanceUUID,
            @Nullable String instanceHost,
            @Nullable String instanceServer,
            @Nullable Integer instancePort
    ) {
        Set<ConnectionDTO> filtered = connectionService.findAll().stream()
                .filter(c -> targetId == null || Objects.equals(c.getTarget().getId(), targetId))
                .filter(c -> targetType == null || Objects.equals(c.getTarget().getType(), targetType))
                .filter(c -> instanceUUID == null || Objects.equals(c.getInstance().getUuid(), instanceUUID))
                .filter(c -> instanceHost == null || Objects.equals(c.getInstance().getHost(), instanceHost))
                .filter(c -> instanceServer == null || Objects.equals(c.getInstance().getServer(), instanceServer))
                .filter(c -> instancePort == null || Objects.equals(c.getInstance().getPort(), instancePort))
                .map(connectionMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(filtered);
    }

    @Override
    public ResponseEntity<ConnectionDTO> postConnection(ConnectionDTO dto) {
        Connection converted = connectionMapper.toEntity(dto);
        Connection created = connectionService.create(converted);
        ConnectionDTO createdDTO = connectionMapper.toDTO(created);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdDTO);
    }

    @Override
    public ResponseEntity<ConnectionDTO> getConnectionByTarget(TargetType target, Long id) {
        EntityTarget entityTarget = new EntityTarget(id, target);
        Optional<ConnectionDTO> connection = connectionService.findByTarget(entityTarget).map(connectionMapper::toDTO);
        return ResponseEntity.of(connection);
    }

    @Override
    public ResponseEntity<Void> deleteConnectionByTarget(TargetType target, Long id) {
        EntityTarget entityTarget = new EntityTarget(id, target);
        connectionService.deleteByTarget(entityTarget);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ConnectionDTO> getConnectionByInstanceUUID(UUID uuid) {
        Optional<ConnectionDTO> connection = connectionService.findByInstanceUUID(uuid).map(connectionMapper::toDTO);
        return ResponseEntity.of(connection);
    }

    @Override
    public ResponseEntity<Void> deleteConnectionByInstanceUUID(UUID uuid) {
        connectionService.deleteByInstanceUUID(uuid);
        return ResponseEntity.noContent().build();
    }
}