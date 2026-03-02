package de.fhdw.vendix.commons.spring.core.crud;

import de.fhdw.vendix.commons.api.structure.entity.Identifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;

public abstract class AbstractSpringDataCrudLogAdapter<T extends Identifiable<ID>, ID> extends AbstractSpringDataCrudAdapter<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(AbstractSpringDataCrudLogAdapter.class);

    protected AbstractSpringDataCrudLogAdapter(CrudRepository<T, ID> repository) {
        super(repository);
    }

    @Override
    protected void afterCreate(T entity) {
        log.atInfo().log("[CREATE] ENTITY {}", entity);
    }

    @Override
    protected void afterUpdate(T entity) {
        log.atInfo().log("[UPDATE] ENTITY {}", entity);
    }

    @Override
    protected void afterDeleteById(ID id) {
        log.atInfo().log("[DELETE] ID {}", id);
    }

    @Override
    protected void afterDeleteEntity(T entity) {
        log.atInfo().log("[DELETE] ENTITY {}", entity);
    }

    @Override
    protected void afterDeleteAll() {
        log.atInfo().log("[DELETE] ALL");
    }
}