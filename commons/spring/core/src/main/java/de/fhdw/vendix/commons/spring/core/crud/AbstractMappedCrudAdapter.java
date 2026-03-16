package de.fhdw.vendix.commons.spring.core.crud;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import de.fhdw.vendix.commons.api.structure.entity.Identifiable;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.api.structure.port.CrudCommandPort;
import de.fhdw.vendix.commons.api.structure.port.CrudQueryPort;
import jakarta.transaction.Transactional;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public abstract class AbstractMappedCrudAdapter<T extends Identifiable<ID>, ID, D extends Record & DomainDTO>
        implements CrudQueryPort<D, ID>, CrudCommandPort<D, ID> {

    private final ListCrudRepository<T, ID> repository;
    private final EntityMapper<T, D> mapper;

    protected AbstractMappedCrudAdapter(ListCrudRepository<T, ID> repository, EntityMapper<T, D> mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public boolean existsById(ID id) {
        return false;
    }

    @Override
    public Optional<D> findById(ID id) {
        return Optional.empty();
    }

    @Override
    public Iterable<D> findAll() {
        return null;
    }

    @Override
    public Iterable<D> findAllById(Iterable<ID> ids) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public D create(D entity) {
        return null;
    }

    @Override
    @Transactional
    public Iterable<D> createAll(Iterable<D> entities) {
        return null;
    }

    @Override
    public D update(D entity) {
        return null;
    }

    @Override
    @Transactional
    public Iterable<D> updateAll(Iterable<D> entities) {
        return null;
    }

    @Override
    public void delete(D entity) {

    }

    @Override
    public void deleteById(ID id) {

    }

    @Override
    @Transactional
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    @Transactional
    public void deleteAll(Iterable<D> entities) {
        repository.deleteAll();
    }

    @Override
    @Transactional
    public void deleteAllById(Iterable<ID> ids) {

    }
}