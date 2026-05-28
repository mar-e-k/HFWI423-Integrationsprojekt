package de.fhdw.vendix.commons.spring.data.persistance.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.jspecify.annotations.Nullable;

@MappedSuperclass
public abstract class AbstractSpringDataEntity<ID> {

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
}