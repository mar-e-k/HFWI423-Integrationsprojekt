package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.entity.StoreLinkStock;
import de.fhdw.fillialensystem.persistence.entity.imported.Article;
import de.fhdw.fillialensystem.persistence.repository.StoreLinkStockRepository;
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
