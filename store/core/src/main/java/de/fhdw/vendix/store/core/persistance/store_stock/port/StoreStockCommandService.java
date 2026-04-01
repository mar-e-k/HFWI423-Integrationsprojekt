package de.fhdw.vendix.store.core.persistance.store_stock.port;

import de.fhdw.vendix.commons.api.domain.store_stock.StoreStockDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudCommandService;

interface StoreStockCommandService extends CrudCommandService<StoreStockDTO, Long> {
    void restockArticle(Long storeID, Long articleID, Long articleQuantity);
}