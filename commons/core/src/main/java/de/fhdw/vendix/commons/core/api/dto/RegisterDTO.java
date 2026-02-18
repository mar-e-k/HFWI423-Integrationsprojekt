package de.fhdw.vendix.commons.core.api.dto;

public class RegisterDTO extends AbstractDTO<Long> {

    private Long store;

    public RegisterDTO() {
        super();
    }

    public RegisterDTO(Long id) {
        super(id);
    }

    public RegisterDTO(Long id, Long store) {
        super(id);
        this.store = store;
    }

    public Long getStore() {
        return store;
    }

    public void setStore(Long store) {
        this.store = store;
    }
}