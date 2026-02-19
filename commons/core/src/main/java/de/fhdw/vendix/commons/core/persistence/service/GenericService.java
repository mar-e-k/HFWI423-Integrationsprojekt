package de.fhdw.vendix.commons.core.persistence.service;

import de.fhdw.vendix.commons.core.persistence.entity.GenericEntity;

import java.util.List;
import java.util.Optional;

public interface GenericService<T extends GenericEntity<ID>, ID> {
    long count();
    List<T> findAll();
    Optional<T> findById(ID id);
    T create(T entity);
    T update(T entity);
    void delete(ID id);
}