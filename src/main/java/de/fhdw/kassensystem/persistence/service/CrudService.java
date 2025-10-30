package de.fhdw.kassensystem.persistence.service;

import org.springframework.data.repository.CrudRepository;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Validated
public abstract class CrudService<T, ID> { //Note: might not want to add CUD Operations. If only read: class name doesn't make sense and can be changed

    private final CrudRepository<T, ID> repository;

    public CrudService(CrudRepository<T, ID> repository) {
        this.repository = repository;
    }

    public List<T> findAll() {
        return StreamSupport.stream(repository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }
}