package de.fhdw.vendix.orchestrator.web.controller;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.spring.web.api.orchestrator.RegisterApi;
import de.fhdw.vendix.orchestrator.core.domain.register.RegisterMapper;
import de.fhdw.vendix.orchestrator.core.domain.register.RegisterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
class RegisterController implements RegisterApi {

    private final RegisterService registerService;
    private final RegisterMapper registerMapper;

    RegisterController(RegisterService registerService, RegisterMapper registerMapper) {
        this.registerService = registerService;
        this.registerMapper = registerMapper;
    }

    @Override
    public ResponseEntity<List<RegisterDTO>> getRegisters() {
        List<RegisterDTO> registers = registerService.findAll().stream()
                .map(registerMapper::toDTO)
                .toList();
        return ResponseEntity.ok(registers);
    }

    @Override
    public ResponseEntity<RegisterDTO> getRegisterById(Long registerId) {
        Optional<RegisterDTO> register = registerService.findById(registerId).map(registerMapper::toDTO);
        return ResponseEntity.of(register);
    }

    @Override
    public ResponseEntity<List<RegisterDTO>> getLockedRegisters() {
        List<RegisterDTO> registers = registerService.findAllLockedRegisters().stream()
                .map(registerMapper::toDTO)
                .toList();
        return ResponseEntity.ok(registers);
    }

    @Override
    public ResponseEntity<List<RegisterDTO>> getNonLockedRegisters() {
        List<RegisterDTO> registers = registerService.findAllNonLockedRegisters().stream()
                .map(registerMapper::toDTO)
                .toList();
        return ResponseEntity.ok(registers);
    }

    @Override
    public ResponseEntity<List<RegisterDTO>> getStoreRegisters(Long storeId) {
        List<RegisterDTO> registers = registerService.findAllByStoreId(storeId).stream()
                .map(registerMapper::toDTO)
                .toList();
        return ResponseEntity.ok(registers);
    }

    @Override
    public ResponseEntity<List<RegisterDTO>> getLockedStoreRegisters(Long storeId) {
        List<RegisterDTO> registers = registerService.findAllLockedRegistersByStoreId(storeId).stream()
                .map(registerMapper::toDTO)
                .toList();
        return ResponseEntity.ok(registers);
    }

    @Override
    public ResponseEntity<List<RegisterDTO>> getNonLockedStoreRegisters(Long storeId) {
        List<RegisterDTO> registers = registerService.findAllNonLockedRegistersByStoreId(storeId).stream()
                .map(registerMapper::toDTO)
                .toList();
        return ResponseEntity.ok(registers);
    }
}
