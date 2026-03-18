package de.fhdw.vendix.commons.spring.core.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

@MappedSuperclass
public abstract class AbstractSpringDataVersioningEntity<ID> extends AbstractSpringDataEntity<ID> {

    @Version
    private Long version;

    protected AbstractSpringDataVersioningEntity() {}

    protected AbstractSpringDataVersioningEntity(ID id) {
        super(id);
    }
}