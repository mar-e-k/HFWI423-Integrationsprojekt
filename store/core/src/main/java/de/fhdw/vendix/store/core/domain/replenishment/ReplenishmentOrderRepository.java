package de.fhdw.vendix.store.core.domain.replenishment;

import de.fhdw.vendix.commons.api.domain.replenishment.ReplenishmentOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

interface ReplenishmentOrderRepository extends JpaRepository<ReplenishmentOrder, Long> {

    Optional<ReplenishmentOrder> findByCorrelationId(UUID correlationId);

    Optional<ReplenishmentOrder> findFirstByStoreIdAndArticleIdAndStatusInOrderByCreatedAtAsc(
            Long storeId,
            Long articleId,
            Collection<ReplenishmentOrderStatus> statuses
    );
}
