package de.fhdw.vendix.store.core.domain.store_stock.service;

import de.fhdw.vendix.commons.api.domain.store_stock.StoreStockDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudQueryService;

import java.util.Optional;

interface StoreStockQueryService extends CrudQueryService<StoreStockDTO, Long> {
    Optional<StoreStockDTO> findByStoreIDAndArticleID(Long storeID, Long articleID);
}