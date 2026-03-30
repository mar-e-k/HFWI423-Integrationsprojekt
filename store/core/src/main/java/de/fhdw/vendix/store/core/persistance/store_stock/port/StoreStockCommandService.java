package de.fhdw.vendix.store.core.persistance.store_stock.port;

import de.fhdw.vendix.commons.api.structure.service.CommandService;

interface StoreStockCommandService extends CommandService {
    void restockArticle(Long storeID, Long articleID, Long articleQuantity);
}