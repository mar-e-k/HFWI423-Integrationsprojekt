package de.fhdw.vendix.orchestrator.core.embeddable.entity_target;

import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.api.structure.mapper.Default;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Embeddable
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "unique_target", columnNames = {"target_id", "target_type"})
})
@SuppressWarnings("NullAway")
public class EntityTarget {

    @Column(nullable = false, updatable = false, name = "target_id")
    @Min(value = 1, message = "ID must be at least 1")
    @NotNull(message = "ID cannot be null")
    private Long id;

    @Column(nullable = false, updatable = false, name = "target_type")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Type cannot be null")
    private TargetType type;

    public EntityTarget() {}

    @Default
    public EntityTarget(Long id, TargetType type) {
        this.id = id;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public TargetType getType() {
        return type;
    }
}