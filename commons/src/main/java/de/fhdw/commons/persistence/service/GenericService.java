package de.fhdw.commons.persistence.service;

import de.fhdw.commons.persistence.entity.GenericEntity;

import java.util.List;
import java.util.Optional;

public interface GenericService<T extends GenericEntity<ID>, ID> {
    List<T> findAll();
    Optional<T> findById(ID id);
    T create(T entity);
    T update(ID id, T updateToEntity);
    T update(T entity);
    void delete(T entity);
    void delete(ID id);
}