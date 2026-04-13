package de.fhdw.vendix.commons.spring.data.crud;

import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataEntity;
import de.fhdw.vendix.commons.spring.data.service.CrudService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class AbstractCrudService<ENT extends AbstractSpringDataEntity<ID>, ID> extends OperationHook implements CrudService<ENT, ID> {

    private final JpaRepository<ENT, ID> repository;

    protected AbstractCrudService(JpaRepository<ENT, ID> repository) {
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
    public List<ENT> createAll(Iterable<ENT> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Parameter 'entities' cannot be null");
        }

        for (ENT entity : entities) {
            if (entity == null) {
                throw new IllegalArgumentException("Parameter 'entities' contains null 'entity'");
            }
            beforeCreate(entity);
        }
        List<ENT> created = repository.saveAll(entities);
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
    public List<ENT> updateAll(Iterable<ENT> entities) {
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
        List<ENT> updated = repository.saveAll(entities);
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
        List<ENT> deleted = repository.findAll();

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

        entities.forEach(this::beforeDelete);
        repository.deleteAll(entities);
        entities.forEach(this::afterDelete);
    }

    @Override
    @Transactional
    public void deleteAllById(Iterable<ID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("Parameter 'ids' cannot be null");
        }

        List<ENT> deleted = findAllById(ids);

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

        List<ENT> found = repository.findAllById(ids);
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
    public List<ENT> findAll() {
        return repository.findAll();
    }

    @Override
    public Page<ENT> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ENT> findAllById(Iterable<ID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("Parameter 'ids' cannot be null");
        }
        return repository.findAllById(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ENT> findAllByIdNotIn(Iterable<ID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("Parameter 'ids' cannot be null");
        }

        Set<ID> excludedIds = StreamSupport.stream(ids.spliterator(), false)
                .collect(Collectors.toSet());

        return repository.findAll().stream()
                .filter(entity -> !excludedIds.contains(entity.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }
}