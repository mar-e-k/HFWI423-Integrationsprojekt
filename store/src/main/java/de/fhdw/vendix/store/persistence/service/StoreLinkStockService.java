package de.fhdw.vendix.store.persistence.service;

import de.fhdw.vendix.store.persistence.entity.Store;
import de.fhdw.vendix.store.persistence.entity.StoreLinkStock;
import de.fhdw.vendix.store.persistence.entity.imported.Article;
import de.fhdw.vendix.store.persistence.repository.StoreLinkStockRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StoreLinkStockService extends AbstractCrudService<StoreLinkStock, Long> {

    public StoreLinkStockService(StoreLinkStockRepository storeLinkStockRepository) {
        super(storeLinkStockRepository);
    }

    public boolean stockExistsForStore(Store store) {
        return ((StoreLinkStockRepository) repository).existsByStore(store);
    }

    public void saveAll(List<StoreLinkStock> storeLinkStocks) {
        ((StoreLinkStockRepository) repository).saveAll(storeLinkStocks);
    }

    public Optional<StoreLinkStock> findByStoreAndArticle(Store store, Article article) {
        return ((StoreLinkStockRepository) repository).findByStoreAndArticle(store, article);
    }
}
