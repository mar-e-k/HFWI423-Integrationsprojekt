package de.fhdw.commons.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;

public class AuthorityDTO extends AbstractDTO<String> {

    public AuthorityDTO() {
        super();
    }

    public AuthorityDTO(AccountRoleEnum role) {
        super("ROLE_".concat(role.name()));
    }

    @JsonProperty("authority")
    @Override
    public String getId() {
        return super.getId();
    }

    @JsonProperty("authority")
    @Override
    public void setId(String s) {
        super.setId(s);
    }
}