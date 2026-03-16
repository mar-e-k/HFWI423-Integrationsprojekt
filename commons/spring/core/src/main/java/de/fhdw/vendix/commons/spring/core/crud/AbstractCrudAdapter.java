package de.fhdw.vendix.commons.spring.core.crud;

import de.fhdw.vendix.commons.api.structure.entity.Identifiable;
import de.fhdw.vendix.commons.api.structure.port.CrudCommandPort;
import de.fhdw.vendix.commons.api.structure.port.CrudQueryPort;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

public abstract class AbstractCrudAdapter<T extends Identifiable<ID>, ID>
        implements CrudQueryPort<T, ID>, CrudCommandPort<T, ID> {

    private final ListCrudRepository<T, ID> repository;

    protected AbstractCrudAdapter(ListCrudRepository<T, ID> repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Parameter 'repository' cannot be null");
        }
        this.repository = repository;
    }

    @Override
    @Transactional
    public T create(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Parameter 'entity' cannot be null");
        }

        ID id = entity.getIdentifiable();
        if (id != null && repository.existsById(id)) {
            throw new EntityExistsException("Cannot create entity. Entity with ID '%s' already exists".formatted(id));
        }

        beforeCreate(entity);
        T created = repository.save(entity);
        afterCreate(created);

        return created;
    }

    @Override
    @Transactional
    public List<T> createAll(Iterable<T> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Parameter 'entities' cannot be null");
        }

        for (T entity : entities) {
            if (entity == null) {
                throw new IllegalArgumentException("Parameter 'entities' contains null 'entity'");
            }
            beforeCreate(entity);
        }
        List<T> created = repository.saveAll(entities);
        created.forEach(this::afterCreate);

        return created;
    }

    @Override
    @Transactional
    public T update(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Parameter 'entity' cannot be null");
        }

        ID id = entity.getIdentifiable();
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'entity' contains a null id. Cannot update");
        }
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Cannot update entity. Entity with ID '%s' does not exist.".formatted(id));
        }

        beforeUpdate(entity);
        T updated = repository.save(entity);
        afterUpdate(updated);

        return updated;
    }

    @Override
    @Transactional
    public List<T> updateAll(Iterable<T> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Parameter 'entities' cannot be null");
        }

        for (T entity : entities) {
            if (entity == null) {
                throw new IllegalArgumentException("Parameter 'entities' contains a null 'entity'");
            }
            if (!exists(entity)) {
                throw new IllegalArgumentException("Cannot update entity. Entity '%s' does not exist".formatted(entity));
            }
            beforeUpdate(entity);
        }
        List<T> updated = repository.saveAll(entities);
        updated.forEach(this::afterUpdate);

        return updated;
    }

    @Override
    @Transactional
    public void delete(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Parameter 'entity' cannot be null");
        }

        beforeDelete(entity);
        repository.delete(entity);
        afterDelete(entity);
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'id' cannot be null");
        }

        T deleted = repository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        beforeDelete(deleted);
        repository.deleteById(id);
        afterDelete(deleted);
    }

    @Override
    @Transactional
    public void deleteAll() {
        List<T> deleted = repository.findAll();

        deleted.forEach(this::beforeDelete);
        repository.deleteAll();
        deleted.forEach(this::afterDelete);
    }

    @Override
    @Transactional
    public void deleteAll(Iterable<T> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Parameter 'entities' cannot be null");
        }

        List<T> deleted = findAll(entities);

        deleted.forEach(this::beforeDelete);
        repository.deleteAll(entities);
        deleted.forEach(this::afterDelete);
    }

    @Override
    @Transactional
    public void deleteAllById(Iterable<ID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("Parameter 'ids' cannot be null");
        }

        List<T> deleted = findAllById(ids);

        deleted.forEach(this::beforeDelete);
        repository.deleteAllById(ids);
        deleted.forEach(this::afterDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(T entity) {
        if (entity == null) {
            return false;
        }

        ID id = entity.getIdentifiable();
        if (id == null) {
            return false;
        }

        return existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(ID id) {
        if (id == null) {
            return false;
        }

        return repository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsAll(Iterable<T> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Parameter 'entities' cannot be null");
        }

        List<ID> ids = new ArrayList<>();
        for (T entity : entities) {
            if (entity == null) {
                return false;
            }

            ID id = entity.getIdentifiable();
            if (id == null) {
                return false;
            }

            ids.add(id);
        }

        return existsAllById(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsAllById(Iterable<ID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("Parameter 'ids' cannot be null");
        }

        List<T> found = repository.findAllById(ids);
        return found.size() == StreamSupport.stream(ids.spliterator(), false).count();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> find(T entity) {
        if (entity == null) {
            return Optional.empty();
        }

        ID id = entity.getIdentifiable();
        if (id == null) {
            return Optional.empty();
        }

        return findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findById(ID id) {
        if (id == null) {
            return Optional.empty();
        }

        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll(Iterable<T> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Parameter 'entities' cannot be null");
        }

        List<ID> ids = extractIds(entities);
        return findAllById(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAllById(Iterable<ID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("Parameter 'ids' cannot be null");
        }
        return repository.findAllById(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }

    private List<ID> extractIds(Iterable<T> entities) {
        List<ID> ids = new ArrayList<>();
        for (T entity : entities) {
            if (entity == null) {
                throw new IllegalArgumentException("Parameter 'entities' contains a null entity");
            }

            ID id = entity.getIdentifiable();
            if (id == null) {
                throw new IllegalArgumentException(
                        "Parameter 'entities' contains an entity with a null id: '%s'".formatted(entity)
                );
            }
            ids.add(id);
        }
        return ids;
    }

    protected void beforeCreate(T entity) {}

    protected void afterCreate(T entity) {}

    protected void beforeUpdate(T entity) {}

    protected void afterUpdate(T entity) {}

    protected void beforeDelete(T entity) {}

    protected void afterDelete(T entity) {}
}