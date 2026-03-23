package de.fhdw.vendix.commons.spring.data.entity;

import de.fhdw.vendix.commons.api.structure.entity.Identifiable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.jspecify.annotations.Nullable;

@MappedSuperclass
public abstract class AbstractSpringDataEntity<ID> implements Identifiable<ID> {

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
    public @Nullable ID getIdentifiable() {
        return id;
    }
}