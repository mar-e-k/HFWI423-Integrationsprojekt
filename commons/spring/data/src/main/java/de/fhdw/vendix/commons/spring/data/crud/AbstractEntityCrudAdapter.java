package de.fhdw.vendix.commons.spring.data.crud;

import de.fhdw.vendix.commons.api.structure.service.CrudCommandService;
import de.fhdw.vendix.commons.api.structure.service.CrudQueryService;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataEntity;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class AbstractEntityCrudAdapter<ENT extends AbstractSpringDataEntity<ID>, ID>
        extends OperationHook
        implements CrudQueryService<ENT, ID>, CrudCommandService<ENT, ID> {

    private final JpaRepository<ENT, ID> repository;

    protected AbstractEntityCrudAdapter(JpaRepository<ENT, ID> repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ENT create(ENT entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Parameter 'entity' cannot be null");
        }

        ID id = entity.getId();
        if (id != null && repository.existsById(id)) {
            throw new EntityExistsException("Cannot create entity. Entity with ID '%s' already exists".formatted(id));
        }

        beforeCreate(entity);
        ENT created = repository.save(entity);
        afterCreate(created);

        return created;
    }

    @Override
    @Transactional
    public Set<ENT> createAll(Iterable<ENT> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Parameter 'entities' cannot be null");
        }

        for (ENT entity : entities) {
            if (entity == null) {
                throw new IllegalArgumentException("Parameter 'entities' contains null 'entity'");
            }
            beforeCreate(entity);
        }
        Set<ENT> created = repository.saveAll(entities).stream().collect(Collectors.toUnmodifiableSet());
        created.forEach(this::afterCreate);

        return created;
    }

    @Override
    @Transactional
    public ENT update(ENT entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Parameter 'entity' cannot be null");
        }

        ID id = entity.getId();
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'entity' contains a null id. Cannot update");
        }
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Cannot update entity. Entity with ID '%s' does not exist.".formatted(id));
        }

        beforeUpdate(entity);
        ENT updated = repository.save(entity);
        afterUpdate(updated);

        return updated;
    }

    @Override
    @Transactional
    public Set<ENT> updateAll(Iterable<ENT> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Parameter 'entities' cannot be null");
        }

        for (ENT entity : entities) {
            if (entity == null) {
                throw new IllegalArgumentException("Parameter 'entities' contains a null 'entity'");
            }
            if (!exists(entity)) {
                throw new IllegalArgumentException("Cannot update entity. Entity '%s' does not exist".formatted(entity));
            }
            beforeUpdate(entity);
        }
        Set<ENT> updated = repository.saveAll(entities).stream().collect(Collectors.toUnmodifiableSet());
        updated.forEach(this::afterUpdate);

        return updated;
    }

    @Override
    @Transactional
    public void delete(ENT entity) {
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

        ENT deleted = repository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        beforeDelete(deleted);
        repository.deleteById(id);
        afterDelete(deleted);
    }

    @Override
    @Transactional
    public void deleteAll() {
        Set<ENT> deleted = repository.findAll().stream().collect(Collectors.toUnmodifiableSet());

        deleted.forEach(this::beforeDelete);
        repository.deleteAll();
        deleted.forEach(this::afterDelete);
    }

    @Override
    @Transactional
    public void deleteAll(Iterable<ENT> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Parameter 'entities' cannot be null");
        }

        Set<ENT> deleted = findAll(entities);

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

        Set<ENT> deleted = findAllById(ids);

        deleted.forEach(this::beforeDelete);
        repository.deleteAllById(ids);
        deleted.forEach(this::afterDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(ENT entity) {
        if (entity == null) {
            return false;
        }

        ID id = entity.getId();
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
    public boolean existsAll(Iterable<ENT> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Parameter 'entities' cannot be null");
        }

        Set<ID> ids = new HashSet<>();
        for (ENT entity : entities) {
            if (entity == null) {
                return false;
            }

            ID id = entity.getId();
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

        Set<ENT> found = repository.findAllById(ids).stream().collect(Collectors.toUnmodifiableSet());
        return found.size() == StreamSupport.stream(ids.spliterator(), false).count();
    }


    @Override
    @Transactional(readOnly = true)
    public boolean existsAllByIdNotIn(Iterable<ID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("Parameter 'ids' cannot be null");
        }

        Set<ID> excludedIds = StreamSupport.stream(ids.spliterator(), false).collect(Collectors.toSet());

        return repository.findAll().stream()
                .anyMatch(entity -> !excludedIds.contains(entity.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ENT> find(ENT entity) {
        if (entity == null) {
            return Optional.empty();
        }

        ID id = entity.getId();
        if (id == null) {
            return Optional.empty();
        }

        return findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ENT> findById(ID id) {
        if (id == null) {
            return Optional.empty();
        }

        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<ENT> findAll() {
        return repository.findAll().stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<ENT> findAll(Iterable<ENT> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Parameter 'entities' cannot be null");
        }

        Set<ID> ids = extractIds(entities);
        return findAllById(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<ENT> findAllById(Iterable<ID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("Parameter 'ids' cannot be null");
        }
        return repository.findAllById(ids).stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<ENT> findAllByIdNotIn(Iterable<ID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("Parameter 'ids' cannot be null");
        }

        Set<ID> excludedIds = StreamSupport.stream(ids.spliterator(), false)
                .collect(Collectors.toSet());

        return repository.findAll().stream()
                .filter(entity -> !excludedIds.contains(entity.getId()))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }

    private Set<ID> extractIds(Iterable<ENT> entities) {
        Set<ID> ids = new HashSet<>();
        for (ENT entity : entities) {
            if (entity == null) {
                throw new IllegalArgumentException("Parameter 'entities' contains a null entity");
            }

            ID id = entity.getId();
            if (id == null) {
                throw new IllegalArgumentException(
                        "Parameter 'entities' contains an entity with a null id: '%s'".formatted(entity)
                );
            }
            ids.add(id);
        }
        return ids;
    }
}