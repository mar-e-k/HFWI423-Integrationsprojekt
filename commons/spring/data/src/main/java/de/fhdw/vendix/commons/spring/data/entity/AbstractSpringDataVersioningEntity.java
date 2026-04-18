package de.fhdw.vendix.commons.spring.data.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

@MappedSuperclass
public abstract class AbstractSpringDataVersioningEntity<ID> extends AbstractSpringDataEntity<ID> {

    @Version
    private Long version = 0L;

    protected AbstractSpringDataVersioningEntity() {}

    protected AbstractSpringDataVersioningEntity(ID id) {
        super(id);
    }

    public Long getVersion() {
        return version;
    }
}