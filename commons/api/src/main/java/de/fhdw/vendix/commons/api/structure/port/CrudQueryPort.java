package de.fhdw.vendix.commons.api.structure.port;

import java.util.Optional;

public interface CrudQueryPort<T, ID> extends QueryPort {

    boolean exists(T entity);

    boolean existsById(ID id);

    Optional<T> findById(ID id);

    Iterable<T> findAll();

    Iterable<T> findAllById(Iterable<ID> ids);

    long count();
}