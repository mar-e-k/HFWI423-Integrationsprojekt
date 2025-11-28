package de.fhdw.fillialensystem.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
public class Account extends AbstractEntity implements UserDetails {

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private AccountRole accountRole;

    @OneToMany(mappedBy = "account")
    private List<Receipt> receipts = new ArrayList<>();

    @Column(unique = true, nullable = false)
    @NotNull(message = "Account uuid cannot be null")
    private String uuid;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Account username must not be blank")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Account password must not be blank")
    private String password;

    public Account() {
        super();
    }

    public Account(AccountRole accountRole, List<Receipt> receipts, String uuid, String username, String password) {
        this.accountRole = accountRole;
        this.receipts = receipts;
        this.uuid = uuid;
        this.username = username;
        this.password = password;
    }

    public AccountRole getAccountRole() {
        return accountRole;
    }

    public void setAccountRole(AccountRole accountRole) {
        this.accountRole = accountRole;
    }

    public List<Receipt> getReceipts() {
        return receipts;
    }

    public void setReceipts(List<Receipt> receipts) {
        this.receipts = receipts;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String accountId) {
        this.uuid = accountId;
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
        return List.of(new SimpleGrantedAuthority("ROLE_".concat(accountRole.getRole().name())));
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
