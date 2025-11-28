package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.commons.persistence.entity.GenericEntity;
import de.fhdw.commons.persistence.service.GenericService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Validated
public abstract class AbstractCrudService<T extends GenericEntity<ID>, ID> implements GenericService<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(AbstractCrudService.class);

    protected final CrudRepository<T, ID> repository;

    public AbstractCrudService(CrudRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Override
    public List<T> findAll() {
        return StreamSupport.stream(repository.findAll().spliterator(), false).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public Optional<T> findById(@NotNull ID id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public T create(@Valid T entity) {
        T saved = repository.save(entity);
        log.atInfo().log("[CREATED] [{}] with id [{}]", AopUtils.getTargetClass(this).getSimpleName(), entity.getId());
        return saved;
    }

    @Transactional
    public T save(@Valid T entity) {
        T saved = repository.save(entity);
        log.atInfo().log("[SAVED] [{}] with id [{}]", AopUtils.getTargetClass(this).getSimpleName(), entity.getId());
        return saved;
    }

    @Override
    @Transactional
    public T update(@NotNull ID id, @Valid T entity) {
        if (entity.getId() == null) {
            entity.setId(id);
        } else if (!Objects.equals(id, entity.getId())) {
            throw new IllegalArgumentException("Entity with id [%s] cannot be updated to have id [%s]".formatted(entity.getId(), id));
        }
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Entity from [%s] with id [%s] does not exist".formatted(AopUtils.getTargetClass(this).getSimpleName(), id));
        }
        T saved = repository.save(entity);
        log.atInfo().log("[UPDATED] [{}] with id [{}]", AopUtils.getTargetClass(this).getSimpleName(), id);
        return saved;
    }

    @Override
    @Transactional
    public T update(@Valid T entity) {
        return update(entity.getId(), entity);
    }

    @Override
    @Transactional
    public void delete(@NotNull ID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Entity from [%s] with id [%s] does not exist".formatted(AopUtils.getTargetClass(this).getSimpleName(), id));
        }

        repository.deleteById(id);
        log.atInfo().log("[DELETED] [{}] with id [{}]", AopUtils.getTargetClass(this).getSimpleName(), id);
    }

    @Override
    @Transactional
    public void delete(@Valid T entity) {
        delete(entity.getId());
    }

    public long count() {
        return repository.count();
    }
}
