package de.fhdw.commons.persistence.entity;

public interface GenericEntity<ID> {
    ID getId();
    void setId(ID id);
}