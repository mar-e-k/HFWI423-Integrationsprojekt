package de.fhdw.vendix.orchestrator.web.controller;

import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.web.server.orchestrator.api.LockApi;
import de.fhdw.vendix.orchestrator.core.domain.lock.service.LockService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
class LockController implements LockApi {

    private final LockService lockService;

    LockController(LockService lockService) {
        this.lockService = lockService;
    }

    @Override
    public ResponseEntity<List<LockDTO>> getLocks(
            @Nullable Long targetId,
            @Nullable TargetType targetType,
            @Nullable UUID instanceUUID
    ) {
        List<LockDTO> filtered = lockService.findAll().stream()
                .filter(l -> Objects.equals(l.target().id(), targetId))
                .filter(l -> Objects.equals(l.target().type(), targetType))
                .filter(l -> Objects.equals(l.instanceUUID(), instanceUUID))
                .toList();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(filtered);
    }

    @Override
    public ResponseEntity<LockDTO> postLock(LockDTO lockDTO) {
        LockDTO created = lockService.create(lockDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }
}