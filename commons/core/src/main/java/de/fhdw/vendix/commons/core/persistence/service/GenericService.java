package de.fhdw.vendix.commons.core.persistence.service;

import de.fhdw.vendix.commons.core.persistence.entity.GenericEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

public interface GenericService<T extends GenericEntity<ID>, ID> {
    long count();
    List<T> findAll();
    Optional<T> findById(@NotNull ID id);
    T create(@Valid T entity);
    T update(@Valid T entity);
    void delete(@NotNull ID id);
}