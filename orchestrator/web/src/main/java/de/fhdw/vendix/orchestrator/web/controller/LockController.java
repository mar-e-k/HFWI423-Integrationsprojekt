package de.fhdw.vendix.orchestrator.web.controller;

import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.web.server.orchestrator.api.LockApi;
import de.fhdw.vendix.orchestrator.core.domain.lock.Lock;
import de.fhdw.vendix.orchestrator.core.domain.lock.LockMapper;
import de.fhdw.vendix.orchestrator.core.domain.lock.LockService;
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
class LockController implements LockApi {

    private final LockService lockService;
    private final LockMapper lockMapper;

    LockController(LockService lockService, LockMapper lockMapper) {
        this.lockService = lockService;
        this.lockMapper = lockMapper;
    }

    @Override
    public ResponseEntity<Set<LockDTO>> getLocks(
            @Nullable Long targetId,
            @Nullable TargetType targetType,
            @Nullable UUID instanceUUID
    ) {
        Set<LockDTO> filtered = lockService.findAll().stream()
                .filter(l -> targetId == null || Objects.equals(l.getTarget().getId(), targetId))
                .filter(l -> targetType == null || Objects.equals(l.getTarget().getType(), targetType))
                .filter(l -> instanceUUID == null || Objects.equals(l.getInstanceUUID(), instanceUUID))
                .map(lockMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(filtered);
    }

    @Override
    public ResponseEntity<LockDTO> postLock(LockDTO lockDTO) {
        Lock converted = lockMapper.toEntity(lockDTO);
        Lock created = lockService.create(converted);
        LockDTO createdDTO = lockMapper.toDTO(created);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdDTO);
    }

    @Override
    public ResponseEntity<LockDTO> getLockByTarget(TargetType target, Long id) {
        EntityTarget entityTarget = new EntityTarget(id, target);
        Optional<LockDTO> lockDTO = lockService.findByTarget(entityTarget).map(lockMapper::toDTO);
        return ResponseEntity.of(lockDTO);
    }

    @Override
    public ResponseEntity<Void> deleteLockByTarget(TargetType target, Long id) {
        EntityTarget entityTarget = new EntityTarget(id, target);
        lockService.deleteByTarget(entityTarget);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteAllLocksByInstanceUUID(UUID uuid) {
        lockService.deleteAllByInstanceUUID(uuid);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Set<LockDTO>> getAllLocksByInstanceUUID(UUID uuid) {
        Set<LockDTO> locks = lockMapper.toDTOs(lockService.findAllByInstanceUUID(uuid));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(locks);
    }
}