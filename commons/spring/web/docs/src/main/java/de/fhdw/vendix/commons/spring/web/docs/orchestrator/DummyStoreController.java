package de.fhdw.vendix.commons.spring.web.docs.orchestrator;

import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.spring.web.api.orchestrator.StoreApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class DummyStoreController implements StoreApi {

    @Override
    public ResponseEntity<List<StoreDTO>> getStores() {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<StoreDTO> getStoreById(Long storeId) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<StoreDTO>> getLockedStores() {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<StoreDTO>> getNonLockedStores() {
        return ResponseEntity.noContent().build();
    }
}
