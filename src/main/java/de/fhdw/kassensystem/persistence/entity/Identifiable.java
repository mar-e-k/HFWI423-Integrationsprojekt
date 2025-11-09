package de.fhdw.kassensystem.persistence.entity;

public interface Identifiable<ID> {
    ID getId();
    void setId(ID id);
}