package de.fhdw.vendix.commons.api.domain.store_stock.port;

import de.fhdw.vendix.commons.api.structure.port.CommandPort;

public interface StoreStockCommandPort extends CommandPort {
    void restockArticle(Long storeID, Long articleID, Long articleQuantity);
}