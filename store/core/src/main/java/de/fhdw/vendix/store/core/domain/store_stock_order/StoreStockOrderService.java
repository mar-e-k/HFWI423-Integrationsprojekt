package de.fhdw.vendix.store.core.domain.store_stock_order;

import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderRequestDTO;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderResponseDTO;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderDTO;

import java.util.Optional;
import java.util.UUID;

public interface StoreStockOrderService {

    StoreStockOrderResponseDTO requestReplenishment(StoreStockOrderRequestDTO request);

    Optional<StoreStockOrderDTO> findStatus(UUID correlationId);

    void markReceived(long storeId, long articleId, long amount);
}
