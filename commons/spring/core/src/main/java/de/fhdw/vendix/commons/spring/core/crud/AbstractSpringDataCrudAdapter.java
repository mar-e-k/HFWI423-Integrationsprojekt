package de.fhdw.vendix.commons.spring.core.crud;

import de.fhdw.vendix.commons.api.structure.entity.Identifiable;
import de.fhdw.vendix.commons.api.structure.port.CrudCommandPort;
import de.fhdw.vendix.commons.api.structure.port.CrudQueryPort;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractSpringDataCrudAdapter<T extends Identifiable<ID>, ID> implements CrudQueryPort<T, ID>, CrudCommandPort<T, ID> {

    protected final CrudRepository<T, ID> repository;

    protected AbstractSpringDataCrudAdapter(CrudRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Override
    public T create(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Parameter 'entity' cannot be null");
        }

        ID id = entity.getId();
        if (id != null && repository.existsById(id)) {
            throw new EntityExistsException("Cannot create entity. Entity with ID '%s' already exists".formatted(id));
        }

        T created = repository.save(entity);
        afterCreate(created);

        return created;
    }

    @Override
    @Transactional
    public Iterable<T> createAll(Iterable<T> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Parameter 'entities' cannot be null");
        }

        List<T> created = new ArrayList<>();
        for (T entity : entities) {
            created.add(this.create(entity));
        }

        return created;
    }

    @Override
    public T update(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Parameter 'entity' cannot be null");
        }

        ID id = entity.getId();
        if (id != null && !repository.existsById(id)) {
            throw new EntityNotFoundException("Cannot update entity. Entity with ID '%s' does not exist.".formatted(id));
        }

        T updated = repository.save(entity);
        afterUpdate(updated);

        return updated;
    }

    @Override
    @Transactional
    public Iterable<T> updateAll(Iterable<T> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Parameter 'entities' cannot be null");
        }

        List<T> updated = new ArrayList<>();
        for (T entity : entities) {
            updated.add(this.update(entity));
        }

        return updated;
    }

    @Override
    public void delete(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Parameter 'entity' cannot be null");
        }

        repository.delete(entity);
        afterDeleteEntity(entity);
    }

    @Override
    public void deleteById(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'id' cannot be null");
        }

        repository.deleteById(id);
        afterDeleteById(id);
    }

    @Override
    @Transactional
    public void deleteAll() {
        repository.deleteAll();
        afterDeleteAll();
    }

    @Override
    @Transactional
    public void deleteAll(Iterable<T> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Parameter 'entities' cannot be null");
        }

        entities.forEach(this::delete);
        afterDeleteAll();
    }

    @Override
    @Transactional
    public void deleteAllById(Iterable<ID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("Parameter 'ids' cannot be null");
        }

        ids.forEach(this::deleteById);
    }

    @Override
    public boolean exists(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Parameter 'entity' cannot be null");
        }
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Parameter 'entity' with field 'id' cannot be null");
        }

        return repository.existsById(entity.getId());
    }

    @Override
    public boolean existsById(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'id' cannot be null");
        }

        return repository.existsById(id);
    }

    @Override
    public Optional<T> findById(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'id' cannot be null");
        }

        return repository.findById(id);
    }

    @Override
    public Iterable<T> findAll() {
        return repository.findAll();
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("Parameter 'ids' cannot be null");
        }

        return repository.findAllById(ids);
    }

    @Override
    public long count() {
        return repository.count();
    }

    protected void afterCreate(T entity) {}

    protected void afterUpdate(T entity) {}

    protected void afterDeleteById(ID id) {}

    protected void afterDeleteEntity(T entity) {}

    protected void afterDeleteAll() {};
}