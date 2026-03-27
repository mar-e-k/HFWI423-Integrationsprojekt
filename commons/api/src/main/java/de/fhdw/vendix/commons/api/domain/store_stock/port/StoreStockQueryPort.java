package de.fhdw.vendix.commons.api.domain.store_stock.port;

import de.fhdw.vendix.commons.api.domain.store_stock.dto.StoreStockDTO;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.util.Optional;

public interface StoreStockQueryPort extends QueryPort {
    Optional<StoreStockDTO> findByStoreIDAndArticleID(Long storeID, Long articleID);
}