package de.fhdw.commons.api.dto;

import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AccountDTO extends AbstractDTO<Long> implements UserDetails {

    private AccountRoleEnum role;
    private String uuid;
    private String username;
    private String password;
    private List<AuthorityDTO> authorities;

    public AccountDTO() {
        super();
    }

    public AccountDTO(Long id, AccountRoleEnum role, String uuid, String username, String password) {
        super(id);
        this.role = role;
        this.uuid = uuid;
        this.username = username;
        this.password = password;
        this.authorities = List.of(new AuthorityDTO(role));
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



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            return List.of();
        }

        return authorities.stream()
                .map(a -> new SimpleGrantedAuthority(a.getId())) //id=role here
                .toList();
    }

    public void setAuthorities(List<AuthorityDTO> authorities) {
        this.authorities = authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
