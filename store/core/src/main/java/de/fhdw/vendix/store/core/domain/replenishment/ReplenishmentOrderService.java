package de.fhdw.vendix.store.core.domain.replenishment;

import de.fhdw.vendix.commons.api.domain.replenishment.ReplenishmentOrderRequestDTO;
import de.fhdw.vendix.commons.api.domain.replenishment.ReplenishmentOrderResponseDTO;
import de.fhdw.vendix.commons.api.domain.replenishment.ReplenishmentOrderStatusDTO;

import java.util.Optional;
import java.util.UUID;

public interface ReplenishmentOrderService {

    ReplenishmentOrderResponseDTO requestReplenishment(ReplenishmentOrderRequestDTO request);

    Optional<ReplenishmentOrderStatusDTO> findStatus(UUID correlationId);

    void markReceived(long storeId, long articleId, long amount);
}
