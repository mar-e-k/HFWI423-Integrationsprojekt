package de.fhdw.vendix.store.core.persistance.store_stock.port;

import de.fhdw.vendix.commons.api.domain.store_stock.StoreStockDTO;
import de.fhdw.vendix.commons.api.structure.service.QueryService;

import java.util.Optional;

interface StoreStockQueryService extends QueryService {
    Optional<StoreStockDTO> findByStoreIDAndArticleID(Long storeID, Long articleID);
}