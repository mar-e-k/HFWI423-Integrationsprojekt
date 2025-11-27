package de.fhdw.commons.api.dto;

import de.fhdw.commons.persistence.entity.AccountRoleEnum;

public class AuthorityDTO extends AbstractDTO<String> {
    public AuthorityDTO(AccountRoleEnum role) {
        super("ROLE_".concat(role.name()));
    }
}