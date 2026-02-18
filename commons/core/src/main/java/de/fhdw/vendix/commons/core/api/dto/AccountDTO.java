package de.fhdw.vendix.commons.core.api.dto;

import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;

public class AccountDTO extends AbstractDTO<Long> {

    private String uuid;
    private String username;
    private String password;
    private AccountRoleEnum role;

    public AccountDTO() {
        super();
    }

    public AccountDTO(Long id, String uuid, String username, String password, AccountRoleEnum role) {
        super(id);
        this.uuid = uuid;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AccountRoleEnum getRole() {
        return role;
    }

    public void setRole(AccountRoleEnum role) {
        this.role = role;
    }
}