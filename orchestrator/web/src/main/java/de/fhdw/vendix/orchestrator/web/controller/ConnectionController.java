package de.fhdw.vendix.orchestrator.web.controller;

import de.fhdw.vendix.commons.api.domain.connection.ConnectionDTO;
import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.web.server.orchestrator.api.ConnectionApi;
import de.fhdw.vendix.orchestrator.core.domain.connection.Connection;
import de.fhdw.vendix.orchestrator.core.domain.connection.ConnectionMapper;
import de.fhdw.vendix.orchestrator.core.domain.connection.ConnectionService;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTarget;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
class ConnectionController implements ConnectionApi {

    private final ConnectionService connectionService;
    private final ConnectionMapper connectionMapper;

    ConnectionController(ConnectionService connectionService, ConnectionMapper connectionMapper) {
        this.connectionService = connectionService;
        this.connectionMapper = connectionMapper;
    }

    @Override
    public ResponseEntity<ConnectionDTO> postConnection(ConnectionDTO dto) {
        Connection converted = connectionMapper.toEntity(dto);
        Connection created = connectionService.create(converted);
        ConnectionDTO createdDTO = connectionMapper.toDTO(created);
        return ResponseEntity.ok(createdDTO);
    }

    @Override
    public ResponseEntity<List<ConnectionDTO>> getConnectionsByType(TargetType type) {
        List<ConnectionDTO> connections = connectionService.findAllByType(type).stream()
                .map(connectionMapper::toDTO)
                .toList();
        return ResponseEntity.ok(connections);
    }

    @Override
    public ResponseEntity<ConnectionDTO> getConnectionByTarget(TargetType target, Long id) {
        EntityTarget entityTarget = new EntityTarget(id, target);
        Optional<ConnectionDTO> connection = connectionService.findByTarget(entityTarget)
                .map(connectionMapper::toDTO);
        return ResponseEntity.of(connection);
    }

    @Override
    public ResponseEntity<ConnectionDTO> getConnectionByInstanceUUID(UUID uuid) {
        Optional<ConnectionDTO> connection = connectionService.findByInstanceUUID(uuid).map(connectionMapper::toDTO);
        return ResponseEntity.of(connection);
    }

    @Override
    public ResponseEntity<Void> deleteConnectionByTarget(TargetType target, Long id) {
        EntityTarget entityTarget = new EntityTarget(id, target);
        connectionService.deleteByTarget(entityTarget);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteConnectionByInstanceUuid(UUID uuid) {
        connectionService.deleteByInstanceUUID(uuid);
        return ResponseEntity.noContent().build();
    }
}