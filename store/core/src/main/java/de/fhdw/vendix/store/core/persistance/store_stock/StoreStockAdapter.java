package de.fhdw.vendix.store.core.persistance.store_stock;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.domain.store_stock.dto.StoreStockDTO;
import de.fhdw.vendix.commons.api.domain.store_stock.port.StoreStockCommandPort;
import de.fhdw.vendix.commons.api.domain.store_stock.port.StoreStockQueryPort;
import de.fhdw.vendix.commons.spring.core.crud.AbstractCrudLogAdapter;
import de.fhdw.vendix.store.core.persistance.article.ArticleMapper;
import de.fhdw.vendix.store.core.persistance.store.StoreMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
class StoreStockAdapter extends AbstractCrudLogAdapter<StoreStock, Long> implements StoreStockQueryPort, StoreStockCommandPort {

   private final StoreStockRepository storeStockRepository;
   private final StoreStockMapper storeStockMapper;
   private final StoreMapper storeMapper;
   private final ArticleMapper articleMapper;

    public StoreStockAdapter(StoreStockRepository storeStockRepository, StoreStockMapper storeStockMapper, StoreMapper storeMapper, ArticleMapper articleMapper) {
        super(storeStockRepository);
        this.storeStockRepository = storeStockRepository;
        this.storeStockMapper = storeStockMapper;
        this.storeMapper = storeMapper;
        this.articleMapper = articleMapper;
    }

    @Override
    public void restockArticle(long storeID, long articleID, long articleQuantity) {
        StoreStock storeStock = storeStockRepository.findByStoreIDAndArticleID(storeID, articleID)
                .orElseThrow(EntityNotFoundException::new);
        storeStock.restockArticle(articleQuantity);
        super.update(storeStock);
    }

    @Override
    public Set<ArticleDTO> findAllArticlesByStore(long storeID) {
        return storeStockRepository.findAllArticlesByStore(storeID).stream()
                .map(articleMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<StoreDTO> findAllStoresByArticle(long articleID) {
        return storeStockRepository.findAllStoresByArticle(articleID).stream()
                .map(storeMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Optional<StoreStockDTO> findByStoreIDAndArticleID(long storeID, long articleID) {
        return storeStockRepository.findByStoreIDAndArticleID(storeID, articleID)
                .map(storeStockMapper::toDTO);
    }
}