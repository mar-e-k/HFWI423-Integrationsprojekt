package de.fhdw.vendix.commons.spring.data.persistance.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CrudService<T, ID> {
    long count();

    boolean exists(T entity);

    boolean existsById(ID id);

    boolean existsAll(Iterable<T> entities);

    boolean existsAllById(Iterable<ID> ids);

    boolean existsAllByIdNotIn(Iterable<ID> ids);

    Optional<T> find(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    Page<T> findAll(Pageable pageable);

    List<T> findAllById(Iterable<ID> ids);

    List<T> findAllByIdNotIn(Iterable<ID> ids);

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