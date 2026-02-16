package de.fhdw.vendix.commons.core.api.dto;

public abstract class AbstractDTO<ID> implements GenericDTO<ID> {

    private ID id;

    public AbstractDTO() {
        super();
    }

    public AbstractDTO(ID id) {
        this.id = id;
    }

    @Override
    public ID getId() {
        return id;
    }

    @Override
    public void setId(ID id) {
        this.id = id;
    }
}
