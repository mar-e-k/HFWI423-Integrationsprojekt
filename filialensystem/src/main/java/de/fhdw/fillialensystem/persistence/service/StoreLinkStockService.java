package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.StoreLinkStock;
import de.fhdw.fillialensystem.persistence.repository.StoreLinkStockRepository;
import org.springframework.stereotype.Service;

@Service
public class StoreLinkStockService extends AbstractCrudService<StoreLinkStock, Long> {

    public StoreLinkStockService(StoreLinkStockRepository storeLinkStockRepository) {
        super(storeLinkStockRepository);
    }
}