package de.fhdw.vendix.commons.core.api.dto;

import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;

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
