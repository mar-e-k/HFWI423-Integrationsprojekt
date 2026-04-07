package de.fhdw.vendix.commons.api.structure.service;

import java.util.Set;

public interface CrudCommandService<T, ID> extends CommandService {
    T create(T entity);

    Set<T> createAll(Iterable<T> entities);

    T update(T entity);

    Set<T> updateAll(Iterable<T> entities);

    void delete(T entity);

    void deleteById(ID id);

    void deleteAll();

    void deleteAll(Iterable<T> entities);

    void deleteAllById(Iterable<ID> ids);
}