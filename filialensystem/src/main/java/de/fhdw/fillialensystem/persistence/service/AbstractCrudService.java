package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.commons.persistence.entity.GenericEntity;
import de.fhdw.commons.persistence.service.GenericService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
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

    public AbstractCrudService(CrudRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Override
    public List<T> findAll() {
        return StreamSupport.stream(repository.findAll().spliterator(), false).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public T create(@Valid T entity) {
        T saved = repository.save(entity);
        log.atInfo().log("[CREATED] [{}] with id [{}]", AopUtils.getTargetClass(this).getSimpleName(), entity.getId());
        return saved;
    }

    @Override
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
                    String.format("Entity from [%s] with id [%s] does not exist", AopUtils.getTargetClass(this).getSimpleName(), id));
        }
        T updatedEntity = repository.save(updateToEntity);
        log.atInfo().log("[UPDATED] [{}] with id [{}]", AopUtils.getTargetClass(this).getSimpleName(), id);
        return updatedEntity;
    }

    @Override
    @Transactional
    public T update(@Valid T entity) {
        return update(entity.getId(), entity);
    }

    @Override
    @Transactional
    public void delete(ID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(
                    String.format("Entity from [%s] with id [%s] does not exist", AopUtils.getTargetClass(this).getSimpleName(), id));
        }

        repository.deleteById(id);
        log.atInfo().log("[DELETED] [{}] with id [{}]", AopUtils.getTargetClass(this).getSimpleName(), id);
    }

    @Override
    @Transactional
    public void delete(@Valid T entity) {
        delete(entity.getId());
    }
}