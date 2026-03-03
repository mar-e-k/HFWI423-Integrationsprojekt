package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.lock.LockEndpoints;
import de.fhdw.vendix.commons.api.domain.lock.dto.LockRequestDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.port.LockCommandPort;
import de.fhdw.vendix.commons.api.domain.lock.port.LockQueryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(LockEndpoints.BASE)
@Tag(name = "Lock", description = "Endpoints for operations related to locks")
public class LockController {

    private final LockCommandPort lockCommandPort;
    private final LockQueryPort lockQueryPort;

    public LockController(LockCommandPort lockCommandPort, LockQueryPort lockQueryPort) {
        this.lockCommandPort = lockCommandPort;
        this.lockQueryPort = lockQueryPort;
    }

    @PostMapping
    @Operation(summary = "Create lock by lockRequestDTO")
    public ResponseEntity<LockDTO> createLock(@RequestBody LockRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lockCommandPort.create(dto));
    }

    @GetMapping(LockEndpoints.BY_TARGET_TYPE_AND_TARGET_ID)
    @Operation(summary = "Find lock by targetType and targetID")
    public ResponseEntity<LockDTO> getByTargetTypeAndTargetId(@PathVariable TargetTypeEnum targetTypeEnum, @PathVariable long targetID) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(lockQueryPort.findByTargetTypeAndTargetID(targetTypeEnum, targetID)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @DeleteMapping(LockEndpoints.BY_TARGET_TYPE_AND_TARGET_ID)
    @Operation(summary = "Delete all locks by targetType and targetID")
    public ResponseEntity<Long> deleteByTargetTypeAndTargetId(@PathVariable TargetTypeEnum targetTypeEnum, @PathVariable long targetID) {
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(lockCommandPort.deleteByTargetTypeAndTargetId(targetTypeEnum, targetID));
    }

    @DeleteMapping(LockEndpoints.BY_INSTANCE_UUID)
    @Operation(summary = "Delete all locks by instanceUUID UUID")
    public ResponseEntity<Long> deleteAllByInstanceId(@PathVariable UUID instanceUUID) {
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(lockCommandPort.deleteAllByInstanceUUID(instanceUUID));
    }
}