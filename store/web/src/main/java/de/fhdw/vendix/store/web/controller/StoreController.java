package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.spring.web.server.store.api.StoreApi;
import de.fhdw.vendix.store.core.domain.register.RegisterMapper;
import de.fhdw.vendix.store.core.domain.store.StoreMapper;
import de.fhdw.vendix.store.core.domain.store.StoreService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
class StoreController implements StoreApi {

    private final StoreService storeService;
    private final StoreMapper storeMapper;
    private final RegisterMapper registerMapper;

    StoreController(StoreService storeService, StoreMapper storeMapper, RegisterMapper registerMapper) {
        this.storeService = storeService;
        this.storeMapper = storeMapper;
        this.registerMapper = registerMapper;
    }

    @Override
    public ResponseEntity<Set<StoreDTO>> getStores(
            @Nullable Long id,
            @Nullable String country,
            @Nullable String city,
            @Nullable String street
    ) {
        Set<StoreDTO> filtered = storeService.findAll().stream()
                .filter(s -> id == null || Objects.equals(s.getId(), id))
                .filter(s -> id == null || Objects.equals(s.getCountry(), country))
                .filter(s -> id == null || Objects.equals(s.getCity(), city))
                .filter(s -> id == null || Objects.equals(s.getStreet(), street))
                .map(storeMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
        return ResponseEntity.ok(filtered);
    }

    @Override
    public ResponseEntity<StoreDTO> getStoreById(Long id) {
        Optional<StoreDTO> store = storeService.findById(id).map(storeMapper::toDTO);
        return ResponseEntity.of(store);
    }

    @Override
    public ResponseEntity<Set<RegisterDTO>> getRegistersByStore(Long id) {
        Set<RegisterDTO> registers = storeService.findAllRegisters(id).stream()
                .map(registerMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
        return ResponseEntity.ok(registers);
    }
}