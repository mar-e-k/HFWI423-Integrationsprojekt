package de.fhdw.kassensystem.rest.dto;

public interface GenericDTO<ID> {
    ID getId();
    void setId(ID id);
}