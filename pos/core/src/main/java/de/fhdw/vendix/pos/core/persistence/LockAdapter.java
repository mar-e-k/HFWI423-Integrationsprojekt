package de.fhdw.vendix.pos.core.persistence;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.domain.lock.web.LockCommandApi;
import de.fhdw.vendix.commons.api.domain.lock.web.LockQueryApi;
import de.fhdw.vendix.commons.spring.web.AbstractApiClient;
import de.fhdw.vendix.security.api.jwt.JwtService;
import de.fhdw.vendix.security.api.jwt.payload.JwtPayload;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
class LockAdapter extends AbstractApiClient implements LockQueryApi, LockCommandApi {

    protected LockAdapter(JwtService jwtService) {
        super(jwtService, "localhost:8080");
    }

    @Override
    protected JwtPayload buildJwtPayload() {
        return null;
    }

    @Override
    public LockDTO create(LockDTO entity) {
        return null;
    }

    @Override
    public void deleteLockByTarget(TargetTypeEnum targetTypeEnum, long targetId) {

    }

    @Override
    public void deleteAllInstanceLocks(UUID instanceUUID) {

    }

    @Override
    public void deleteAllExpiredLocks() {

    }

    @Override
    public boolean existsByTarget(TargetTypeEnum targetTypeEnum, Long id) {
        return false;
    }

    @Override
    public Optional<LockDTO> findByTarget(TargetTypeEnum targetTypeEnum, Long targetID) {
        return Optional.empty();
    }

//    public DistributedLockProxyService(StoreClient storeClient) {
//        super(storeClient);
//    }
//
//    public Mono<Boolean> existsByLockTypeAndTargetId(LockTypeEnum lockType, Long targetId) {
//        return getWebClient()
//                .get()
//                .uri("/api/lock/{lockType}/{targetID}/exists", lockType.name(), targetId)
//                .retrieve()
//                .bodyToMono(Boolean.class)
//                .defaultIfEmpty(false);
//    }
//
//    public Mono<DistributedLockDTO> findByLockTypeAndTargetId(LockTypeEnum lockType, Long targetId) {
//        return getWebClient()
//                .get()
//                .uri("/api/lock/{lockType}/{targetID}", lockType.name(), targetId)
//                .retrieve()
//                .bodyToMono(DistributedLockDTO.class);
//    }
//
//    public Mono<DistributedLockDTO> createDistributedLock(DistributedLockDTO distributedLock) {
//        return getWebClient()
//                .post()
//                .uri("/api/lock")
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .bodyValue(distributedLock)
//                .retrieve()
//                .bodyToMono(DistributedLockDTO.class);
//    }
//
//    public Mono<Long> deleteAllByOwnerInstance(String ownerInstance) {
//        return getWebClient()
//                .delete()
//                .uri("/api/lock/{ownerInstance}", ownerInstance)
//                .retrieve()
//                .bodyToMono(Long.class)
//                .defaultIfEmpty(0L);
//    }
//
//    public Mono<Long> deleteByLockTypeAndTargetId(LockTypeEnum lockType, Long targetId) {
//        return getWebClient()
//                .delete()
//                .uri("/api/lock/{lockType}/{targetID}", lockType.name(), targetId)
//                .retrieve()
//                .bodyToMono(Long.class)
//                .defaultIfEmpty(0L);
//    }
}