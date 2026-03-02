package de.fhdw.vendix.commons.api.structure.entity;

import org.jspecify.annotations.Nullable;

public interface Identifiable<ID> {
    @Nullable ID getId();
}