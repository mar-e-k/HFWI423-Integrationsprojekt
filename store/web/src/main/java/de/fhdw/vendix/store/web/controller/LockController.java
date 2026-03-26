package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.lock.web.LockEndpoints;
import de.fhdw.vendix.commons.api.domain.lock.dto.LockRequestDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.port.LockCommandPort;
import de.fhdw.vendix.commons.api.domain.lock.port.LockQueryPort;
import de.fhdw.vendix.commons.spring.core.mapper.dto.LockDTOMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(LockEndpoints.BASE)
class LockController {

    private final LockDTOMapper lockDTOMapper;
    private final LockCommandPort lockCommandPort;
    private final LockQueryPort lockQueryPort;

    public LockController(LockDTOMapper lockDTOMapper, LockCommandPort lockCommandPort, LockQueryPort lockQueryPort) {
        this.lockDTOMapper = lockDTOMapper;
        this.lockCommandPort = lockCommandPort;
        this.lockQueryPort = lockQueryPort;
    }

    @PostMapping
    public ResponseEntity<LockDTO> createLock(@RequestBody LockRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lockCommandPort.create(lockDTOMapper.toDomainDTO(dto)));
    }

    @GetMapping(LockEndpoints.BY_TARGET_TYPE_AND_TARGET_ID)
    public ResponseEntity<LockDTO> getByTargetTypeAndTargetId(@PathVariable TargetTypeEnum targetTypeEnum, @PathVariable long targetID) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(lockQueryPort.findByTarget(targetTypeEnum, targetID)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @DeleteMapping(LockEndpoints.BY_TARGET_TYPE_AND_TARGET_ID)
    public ResponseEntity<Void> deleteByTargetTypeAndTargetId(@PathVariable TargetTypeEnum targetTypeEnum, @PathVariable long targetID) {
        lockCommandPort.deleteLockByTarget(targetTypeEnum, targetID);
        return ResponseEntity
                .noContent()
                .build();
    }

    @DeleteMapping(LockEndpoints.BY_INSTANCE_UUID)
    public ResponseEntity<Long> deleteAllByInstanceId(@PathVariable UUID instanceUUID) {
        lockCommandPort.deleteAllInstanceLocks(instanceUUID);
        return ResponseEntity
                .noContent()
                .build();
    }
}