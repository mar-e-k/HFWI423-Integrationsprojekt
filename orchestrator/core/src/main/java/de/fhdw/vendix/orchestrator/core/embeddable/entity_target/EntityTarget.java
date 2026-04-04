package de.fhdw.vendix.orchestrator.core.embeddable.entity_target;

import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.api.structure.mapper.Default;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;

@Embeddable
@SuppressWarnings("NullAway")
public class EntityTarget {

    @Column(nullable = false, updatable = false, name = "target_id")
    @Min(0)
    private long id;

    @Column(nullable = false, updatable = false, name = "target_type")
    @Enumerated(EnumType.STRING)
    private TargetType type;


    public EntityTarget() {}

    @Default
    public EntityTarget(long id, TargetType type) {
        this.id = id;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public TargetType getType() {
        return type;
    }
}