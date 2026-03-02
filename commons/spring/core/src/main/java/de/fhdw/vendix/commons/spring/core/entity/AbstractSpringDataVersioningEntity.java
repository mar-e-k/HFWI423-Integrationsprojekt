package de.fhdw.vendix.commons.spring.core.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.jspecify.annotations.Nullable;

@MappedSuperclass
public abstract class AbstractSpringDataVersioningEntity<ID> extends AbstractSpringDataEntity<ID> {

    public AbstractSpringDataVersioningEntity() {}

    public AbstractSpringDataVersioningEntity(@Nullable Long version) {
        this.version = version;
    }

    @Version
    private @Nullable Long version;

    public @Nullable Long getVersion() {
        return version;
    }

    public void setVersion(@Nullable Long version) {
        this.version = version;
    }
}
