package de.fhdw.vendix.store.api.controller;

import de.fhdw.vendix.commons.core.api.dto.DistributedLockDTO;
import de.fhdw.vendix.commons.core.persistence.entity.LockTypeEnum;
import de.fhdw.vendix.store.api.mapper.DistributedLockMapper;
import de.fhdw.vendix.store.persistence.service.DistributedLockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lock")
@Tag(name = "Distributed Lock", description = "Endpoints for operations related to locking")
public class DistributedLockController {

    private final DistributedLockService distributedLockService;
    private final DistributedLockMapper distributedLockMapper;

    public DistributedLockController(DistributedLockService distributedLockService, DistributedLockMapper distributedLockMapper) {
        this.distributedLockService = distributedLockService;
        this.distributedLockMapper = distributedLockMapper;
    }

    @GetMapping("/{lockType}/{targetId}/exists")
    @Operation(summary = "Check if Lock exists by lockType and targetId")
    public ResponseEntity<Boolean> existsByLockTypeAndTargetId(@PathVariable LockTypeEnum lockType, @PathVariable Long targetId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(distributedLockService.existsByLockTypeAndTargetId(lockType, targetId));
    }

    @GetMapping("/{lockType}/{targetId}")
    @Operation(summary = "Retrieve Lock by lockType and targetId")
    public ResponseEntity<DistributedLockDTO> findByLockTypeAndTargetId(@PathVariable LockTypeEnum lockType, @PathVariable Long targetId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(distributedLockMapper.toDto(distributedLockService.findByLockTypeAndTargetId(lockType, targetId)
                        .orElseThrow(EntityNotFoundException::new)));
    }

    @PostMapping
    @Operation(summary = "Create Lock by lockType and targetId")
    public ResponseEntity<DistributedLockDTO> createDistributedLock(@RequestBody DistributedLockDTO distributedLockDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(distributedLockMapper.toDto(
                        distributedLockService.create(
                            distributedLockMapper.toEntity(distributedLockDTO))));
    }

    @DeleteMapping("/{ownerInstance}")
    @Operation(summary = "Delete all locks by owner instance")
    public ResponseEntity<Long> deleteAllByOwnerInstance(@PathVariable String ownerInstance) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(distributedLockService.deleteAllByOwnerInstance(ownerInstance));
    }

    @DeleteMapping("/{lockType}/{targetId}")
    @Operation(summary = "Delete all locks by lock type and targetId")
    public ResponseEntity<Long> deleteByLockTypeAndTargetId(@PathVariable LockTypeEnum lockType, @PathVariable Long targetId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(distributedLockService.deleteByLockTypeAndTargetId(lockType, targetId));
    }
}