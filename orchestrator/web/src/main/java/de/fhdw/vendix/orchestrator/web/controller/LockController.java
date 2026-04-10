package de.fhdw.vendix.orchestrator.web.controller;

import de.fhdw.vendix.commons.api.domain.distributed_lock.DistributedLockDTO;
import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.web.server.orchestrator.api.DistributedLockApi;
import de.fhdw.vendix.orchestrator.core.domain.distributed_lock.DistributedLock;
import de.fhdw.vendix.orchestrator.core.domain.distributed_lock.DistributedLockMapper;
import de.fhdw.vendix.orchestrator.core.domain.distributed_lock.DistributedLockService;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTarget;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
class LockController implements DistributedLockApi {

    private final DistributedLockService distributedLockService;
    private final DistributedLockMapper distributedLockMapper;

    LockController(DistributedLockService distributedLockService, DistributedLockMapper distributedLockMapper) {
        this.distributedLockService = distributedLockService;
        this.distributedLockMapper = distributedLockMapper;
    }

    @Override
    public ResponseEntity<List<DistributedLockDTO>> getAllDistributedLocksByInstanceUUID(UUID uuid) {
        List<DistributedLockDTO> locks = distributedLockMapper.toDTOs(distributedLockService.findAllByInstanceUUID(uuid));
        return ResponseEntity.ok(locks);
    }

    @Override
    public ResponseEntity<DistributedLockDTO> getDistributedLockByTarget(TargetType type, Long id) {
        EntityTarget entityTarget = new EntityTarget(id, type);
        Optional<DistributedLockDTO> lockDTO = distributedLockService.findByTarget(entityTarget).map(distributedLockMapper::toDTO);
        return ResponseEntity.of(lockDTO);
    }

    @Override
    public ResponseEntity<List<DistributedLockDTO>> getDistributedLocksByType(TargetType type) {
        List<DistributedLockDTO> locks = distributedLockService.findAllByTargetType(type).stream()
                .map(distributedLockMapper::toDTO)
                .toList();
        return ResponseEntity.ok(locks);
    }

    @Override
    public ResponseEntity<DistributedLockDTO> postDistributedLock(DistributedLockDTO distributedLockDTO) {
        DistributedLock converted = distributedLockMapper.toEntity(distributedLockDTO);
        DistributedLock created = distributedLockService.create(converted);
        DistributedLockDTO createdDTO = distributedLockMapper.toDTO(created);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdDTO);
    }

    @Override
    public ResponseEntity<Void> deleteAllDistributedLocksByInstanceUUID(UUID uuid) {
        distributedLockService.deleteAllByInstanceUUID(uuid);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteDistributedLockByTarget(TargetType type, Long id) {
        EntityTarget entityTarget = new EntityTarget(id, type);
        distributedLockService.deleteByTarget(entityTarget);
        return ResponseEntity.noContent().build();
    }
}