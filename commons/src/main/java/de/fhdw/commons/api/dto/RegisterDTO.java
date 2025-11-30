package de.fhdw.commons.api.dto;

public class RegisterDTO extends AbstractDTO<Long> {

    private Long storeId;

    public RegisterDTO() {
        super();
    }

    public RegisterDTO(Long id) {
        super(id);
    }

    public RegisterDTO(Long id, Long storeId) {
        super(id);
        this.storeId = storeId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
}