package de.fhdw.vendix.commons.api.domain.store_stock.port;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.domain.store_stock.dto.StoreStockDTO;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.util.Optional;
import java.util.Set;

public interface StoreStockQueryPort extends QueryPort {
    Set<ArticleDTO> findAllArticlesByStore(long storeID);

    Set<StoreDTO> findAllStoresByArticle(long articleID);

    Optional<StoreStockDTO> findByStoreIDAndArticleID(long storeID, long articleID);
}