package de.fhdw.commons.api.dto;

import de.fhdw.commons.persistence.entity.AccountRoleEnum;

public class AccountDTO extends AbstractDTO<Long> {

    private AccountRoleEnum role;
    private String uuid;
    private String username;
    private String password;

    public AccountDTO() {
        super();
    }

    public AccountDTO(Long id, AccountRoleEnum role, String uuid, String username, String password) {
        super(id);
        this.role = role;
        this.uuid = uuid;
        this.username = username;
        this.password = password;
    }

    public AccountRoleEnum getRole() {
        return role;
    }

    public void setRole(AccountRoleEnum role) {
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
}
