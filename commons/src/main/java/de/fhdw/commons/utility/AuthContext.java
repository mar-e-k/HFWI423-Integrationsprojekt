package de.fhdw.commons.utility;

import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

public class AuthContext implements UserDetails, Principal {

    private final AccountRoleEnum accountRole;
    private final String uuid;
    private final String username;
    private final Integer storeId;
    private final Integer registerId;

    public AuthContext(AccountRoleEnum accountRole, String uuid, String username, Integer storeId, Integer registerId) {
        this.accountRole = accountRole;
        this.uuid = uuid;
        this.username = username;
        this.storeId = storeId;
        this.registerId = registerId;
    }

    public AccountRoleEnum getAccountRole() {
        return accountRole;
    }

    public String getUuid() {
        return uuid;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getRegisterId() {
        return registerId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + accountRole.name()));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public String getName() {
        return username;
    }
}
