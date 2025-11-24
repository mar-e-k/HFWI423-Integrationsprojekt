package de.fhdw.commons.persistence.service;

import de.fhdw.commons.persistence.entity.GenericEntity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Validated
public abstract class AbstractCrudService<T extends GenericEntity<ID>, ID> implements GenericService<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(AbstractCrudService.class);
    private final CrudRepository<T, ID> repository;
    private final Class<T> entityClass;

    public AbstractCrudService(CrudRepository<T, ID> repository, Class<T> entityClass) {
        this.repository = repository;
        this.entityClass = entityClass;
    }

    public List<T> findAll() {
        return StreamSupport.stream(repository.findAll().spliterator(), false).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    @Transactional
    public T create(@Valid T entity) {
        T saved = repository.save(entity);
        log.atInfo().log("[CREATED] [{}] with id [{}]", entityClass.getSimpleName(), entity.getId());
        return saved;
    }

    @Transactional
    public T update(ID id, @Valid T updateToEntity) {
        if (updateToEntity.getId() == null) {
            updateToEntity.setId(id);
        } else if (!Objects.equals(id, updateToEntity.getId())) {
            throw new IllegalArgumentException(String.format(
                    "Entity with id [%s] cannot be updated to have id [%s]", updateToEntity.getId(), id));
        }
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(
                    String.format("Entity from [%s] with id [%s] does not exist", entityClass.getSimpleName(), id));
        }
        T updatedEntity = repository.save(updateToEntity);
        log.atInfo().log("[UPDATED] [{}] with id [{}]", entityClass.getSimpleName(), id);
        return updatedEntity;
    }

    @Transactional
    public void delete(ID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(
                    String.format("Entity from [%s] with id [%s] does not exist", entityClass.getSimpleName(), id));
        }

        repository.deleteById(id);
        log.atInfo().log("[DELETED] [{}] with id [{}]", entityClass.getSimpleName(), id);
    }
}