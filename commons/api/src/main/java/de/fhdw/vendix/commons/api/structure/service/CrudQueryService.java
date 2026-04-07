package de.fhdw.vendix.commons.api.structure.service;

import java.util.Optional;
import java.util.Set;

public interface CrudQueryService<T, ID> extends QueryService {

    boolean exists(T entity);

    boolean existsById(ID id);

    boolean existsAll(Iterable<T> entities);

    boolean existsAllById(Iterable<ID> ids);

    boolean existsAllByIdNotIn(Iterable<ID> ids);

    Optional<T> find(T entity);

    Optional<T> findById(ID id);

    Set<T> findAll();

    Set<T> findAll(Iterable<T> entities);

    Set<T> findAllById(Iterable<ID> ids);

    Set<T> findAllByIdNotIn(Iterable<ID> ids);

    long count();
}