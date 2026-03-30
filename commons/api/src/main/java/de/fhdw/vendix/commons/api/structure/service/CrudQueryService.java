package de.fhdw.vendix.commons.api.structure.service;

import de.fhdw.vendix.commons.api.structure.entity.Identifiable;

import java.util.List;
import java.util.Optional;

public interface CrudQueryService<T extends Identifiable<ID>, ID> extends QueryService {

    boolean exists(T entity);

    boolean existsById(ID id);

    boolean existsAll(Iterable<T> entities);

    boolean existsAllById(Iterable<ID> ids);

    Optional<T> find(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    List<T> findAll(Iterable<T> entities);

    List<T> findAllById(Iterable<ID> ids);

    long count();
}