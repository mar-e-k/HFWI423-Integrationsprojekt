package de.fhdw.vendix.commons.api.structure.service;

import java.util.List;

public interface CrudCommandService<T, ID> extends CommandService {
    T create(T entity);

    List<T> createAll(Iterable<T> entities);

    T update(T entity);

    List<T> updateAll(Iterable<T> entities);

    void delete(T entity);

    void deleteById(ID id);

    void deleteAll();

    void deleteAll(Iterable<T> entities);

    void deleteAllById(Iterable<ID> ids);
}