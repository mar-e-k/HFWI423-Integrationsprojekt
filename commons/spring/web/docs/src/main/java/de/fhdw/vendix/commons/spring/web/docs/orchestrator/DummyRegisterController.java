package de.fhdw.vendix.commons.spring.web.docs.orchestrator;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.spring.web.api.orchestrator.RegisterApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class DummyRegisterController implements RegisterApi {

    @Override
    public ResponseEntity<List<RegisterDTO>> getRegisters() {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<RegisterDTO> getRegisterById(Long registerId) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<RegisterDTO>> getLockedRegisters() {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<RegisterDTO>> getNonLockedRegisters() {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<RegisterDTO>> getStoreRegisters(Long storeId) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<RegisterDTO>> getLockedStoreRegisters(Long storeId) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<RegisterDTO>> getNonLockedStoreRegisters(Long storeId) {
        return ResponseEntity.noContent().build();
    }
}