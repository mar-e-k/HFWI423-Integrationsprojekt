package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.entity.StoreLinkStock;
import de.fhdw.fillialensystem.persistence.repository.StoreLinkStockRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreLinkStockService extends AbstractCrudService<StoreLinkStock, Long> {

    private final StoreLinkStockRepository storeLinkStockRepository;

    public StoreLinkStockService(StoreLinkStockRepository storeLinkStockRepository) {
        super(storeLinkStockRepository);
        this.storeLinkStockRepository = storeLinkStockRepository;
    }

    public boolean stockExistsForStore(Store store) {
        return storeLinkStockRepository.existsByStore(store);
    }

    public void saveAll(List<StoreLinkStock> storeLinkStocks) {
        storeLinkStockRepository.saveAll(storeLinkStocks);
    }
}
