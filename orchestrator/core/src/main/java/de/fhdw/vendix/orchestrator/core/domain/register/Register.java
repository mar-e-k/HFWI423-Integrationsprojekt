package de.fhdw.vendix.orchestrator.core.domain.register;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.persistance.entity.AbstractSpringDataAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@Entity
public class Register extends AbstractSpringDataAuditingEntity<Long> {

    @Column(nullable = false)
    @Min(value = 1, message = "Store ID must be at least 1")
    @NotNull(message = "Store ID cannot be null")
    private Long storeId;

    protected Register() {}

    protected Register(Long storeId) {
        this.storeId = storeId;
    }

    @Default
    protected Register(@Nullable Long id, Long storeId) {
        super(id);
        this.storeId = storeId;
    }

    public Long getStoreId() {
        return storeId;
    }
}