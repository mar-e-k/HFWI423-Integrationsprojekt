package de.fhdw.commons.rest.dto;

import de.fhdw.commons.persistence.entity.AccountRoleEnum;

public class AccountRoleDTO extends AbstractDTO<Long> {

    private AccountRoleEnum role;

    public AccountRoleDTO() {
        super();
    }

    public AccountRoleDTO(Long id, AccountRoleEnum role) {
        super(id);
        this.role = role;
    }

    public AccountRoleEnum getRole() {
        return role;
    }

    public void setRole(AccountRoleEnum role) {
        this.role = role;
    }
}
