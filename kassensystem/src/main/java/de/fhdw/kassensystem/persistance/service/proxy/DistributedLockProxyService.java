package de.fhdw.kassensystem.persistance.service.proxy;

import de.fhdw.commons.api.dto.DistributedLockDTO;
import de.fhdw.commons.persistence.entity.LockTypeEnum;
import de.fhdw.kassensystem.utility.StoreClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DistributedLockProxyService extends AbstractProxyService {

    public DistributedLockProxyService(StoreClient storeClient) {
        super(storeClient);
    }

    public Mono<Boolean> existsByLockTypeAndTargetId(LockTypeEnum lockType, Long targetId) {
        return getWebClient()
                .get()
                .uri("/api/lock/{lockType}/{targetId}/exists", lockType.name(), targetId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .defaultIfEmpty(false);
    }

    public Mono<DistributedLockDTO> findByLockTypeAndTargetId(LockTypeEnum lockType, Long targetId) {
        return getWebClient()
                .get()
                .uri("/api/lock/{lockType}/{targetId}", lockType.name(), targetId)
                .retrieve()
                .bodyToMono(DistributedLockDTO.class);
    }

    public Mono<DistributedLockDTO> createDistributedLock(DistributedLockDTO distributedLock) {
        return getWebClient()
                .post()
                .uri("/api/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(distributedLock)
                .retrieve()
                .bodyToMono(DistributedLockDTO.class);
    }

    public Mono<Long> deleteAllByOwnerInstance(String ownerInstance) {
        return getWebClient()
                .delete()
                .uri("/api/lock/{ownerInstance}", ownerInstance)
                .retrieve()
                .bodyToMono(Long.class)
                .defaultIfEmpty(0L);
    }

    public Mono<Long> deleteByLockTypeAndTargetId(LockTypeEnum lockType, Long targetId) {
        return getWebClient()
                .delete()
                .uri("/api/lock/{lockType}/{targetId}", lockType.name(), targetId)
                .retrieve()
                .bodyToMono(Long.class)
                .defaultIfEmpty(0L);
    }
}