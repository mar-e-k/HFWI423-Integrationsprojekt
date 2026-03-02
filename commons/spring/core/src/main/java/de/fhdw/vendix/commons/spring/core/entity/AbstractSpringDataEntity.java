package de.fhdw.vendix.commons.spring.core.entity;

import de.fhdw.vendix.commons.api.structure.entity.Identifiable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.jspecify.annotations.Nullable;

@MappedSuperclass
public abstract class AbstractSpringDataEntity<ID> implements Identifiable<ID> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private ID id;

    protected AbstractSpringDataEntity() {}

    @Override
    public @Nullable ID getId() {
        return id;
    }
}