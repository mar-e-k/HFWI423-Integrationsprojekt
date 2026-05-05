package de.fhdw.vendix.orchestrator.web.controller;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.spring.web.api.orchestrator.StoreApi;
import de.fhdw.vendix.orchestrator.core.domain.store.StoreMapper;
import de.fhdw.vendix.orchestrator.core.domain.store.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
class StoreController implements StoreApi {

    private final StoreService storeService;
    private final StoreMapper storeMapper;

    StoreController(StoreService storeService, StoreMapper storeMapper) {
        this.storeService = storeService;
        this.storeMapper = storeMapper;
    }

    @Override
    public ResponseEntity<List<StoreDTO>> getStores() {
        List<StoreDTO> stores = storeService.findAll().stream()
                .map(storeMapper::toDTO)
                .toList();
        return ResponseEntity.ok(stores);
    }

    @Override
    public ResponseEntity<StoreDTO> getStoreById(Long id) {
        Optional<StoreDTO> store = storeService.findById(id).map(storeMapper::toDTO);
        return ResponseEntity.of(store);
    }

    @Override
    public ResponseEntity<List<StoreDTO>> getLockedStores() {
        List<StoreDTO> stores = storeService.findAllLockedStores().stream()
                .map(storeMapper::toDTO)
                .toList();
        return ResponseEntity.ok(stores);
    }

    @Override
    public ResponseEntity<List<StoreDTO>> getNonLockedStores() {
        List<StoreDTO> stores = storeService.findAllNonLockedStores().stream()
                .map(storeMapper::toDTO)
                .toList();
        return ResponseEntity.ok(stores);
    }

    @Override
    public ResponseEntity<List<RegisterDTO>> getStoreRegisters(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<List<RegisterDTO>> getLockedStoreRegisters(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<List<RegisterDTO>> getNonLockedStoreRegisters(Long id) {
        return null;
    }
}