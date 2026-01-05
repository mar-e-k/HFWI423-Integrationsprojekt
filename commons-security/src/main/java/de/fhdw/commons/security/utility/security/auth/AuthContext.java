package de.fhdw.commons.security.utility.security.auth;

import de.fhdw.commons.api.dto.AccountDTO;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class AuthContext implements UserDetails, Principal, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long accountId;
    private final String uuid;
    private final String username;
    private final String password;
    private final Set<AccountRoleEnum> roles;

    private final Long storeId;
    private final Long registerId;

    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean accountCredentialsNonExpired;
    private final boolean accountEnabled;

    private AuthContext(Builder builder) {
        this.accountId = builder.accountId;
        this.uuid = builder.uuid;
        this.username = builder.username;
        this.password = builder.password;
        this.roles = Set.copyOf(builder.roles);

        this.storeId = builder.storeId;
        this.registerId = builder.registerId;

        this.accountNonExpired = builder.accountNonExpired;
        this.accountNonLocked = builder.accountNonLocked;
        this.accountCredentialsNonExpired = builder.accountCredentialsNonExpired;
        this.accountEnabled = builder.accountEnabled;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public Set<AccountRoleEnum> getRoles() {
        return roles;
    }

    public Long getStoreId() {
        return storeId;
    }

    public Long getRegisterId() {
        return registerId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        return roles.stream()
                .map(Enum::name)
                .map("ROLE_"::concat)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return accountCredentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return accountEnabled;
    }

    public static class Builder {
        private final Long accountId;
        private final String uuid;
        private final String username;
        private final String password;
        private final Set<AccountRoleEnum> roles;

        private Long storeId;
        private Long registerId;

        private boolean accountNonExpired = true;
        private boolean accountNonLocked = true;
        private boolean accountCredentialsNonExpired = true;
        private boolean accountEnabled = true;

        public Builder(AccountDTO account) {
            this.accountId = account.getId();
            this.uuid = account.getUuid();
            this.username = account.getUsername();
            this.password = account.getPassword();
            this.roles = Set.of(account.getRole());
        }

        public Builder(Long accountId, String uuid, String username, String password, Set<AccountRoleEnum> roles) {
            this.accountId = accountId;
            this.uuid = uuid;
            this.username = username;
            this.password = password;
            this.roles = roles;
        }

        public Builder storeId(Long storeId) {
            this.storeId = storeId;
            return this;
        }

        public Builder registerId(Long registerId) {
            this.registerId = registerId;
            return this;
        }

        public Builder accountNonExpired(boolean value) {
            this.accountNonExpired = value;
            return this;
        }

        public Builder accountNonLocked(boolean value) {
            this.accountNonLocked = value;
            return this;
        }

        public Builder accountCredentialsNonExpired(boolean value) {
            this.accountCredentialsNonExpired = value;
            return this;
        }

        public Builder accountEnabled(boolean value) {
            this.accountEnabled = value;
            return this;
        }

        public AuthContext build() {
            return new AuthContext(this);
        }
    }
}