package de.fhdw.vendix.commons.core.persistence.entity;

public interface GenericEntity<ID> {
    ID getId();
    void setId(ID id);
}