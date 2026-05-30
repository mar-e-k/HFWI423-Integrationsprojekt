package de.fhdw.vendix.commons.spring.data.persistance.entity;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

@MappedSuperclass
public abstract class AbstractSpringDataEntity<ID> implements Persistable<ID> {

    @Transient
    private boolean isNewEntity = true;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Nullable
    private ID id;

    protected AbstractSpringDataEntity() {}

    protected AbstractSpringDataEntity(@Nullable ID id) {
        this.id = id;
    }

    public @Nullable ID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return this.isNewEntity;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNewEntity = false;
    }
}