package de.fhdw.fillialensystem.utility;

import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.entity.AccountRole;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.fillialensystem.persistence.service.AccountRoleService;
import de.fhdw.fillialensystem.persistence.service.AccountService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomUserDetails implements UserDetailsService {

    private final AccountService accountService;
    private final AccountRoleService accountRoleService;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetails(AccountService accountService, AccountRoleService accountRoleService, PasswordEncoder passwordEncoder) {
        this.accountService = accountService;
        this.accountRoleService = accountRoleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if ("admin".equals(username) && !hasAdminAccount()) {
            AccountRole adminRole = accountRoleService.findByRole(AccountRoleEnum.ADMIN)
                    .orElseGet(() -> accountRoleService.create(new AccountRole(null, AccountRoleEnum.ADMIN)));

            Account adminAccount = new Account(adminRole, UUID.randomUUID().toString(), "admin", "admin");
            accountService.create(adminAccount);

            return User.builder()
                    .username(adminAccount.getUsername())
                    .password(passwordEncoder.encode(adminAccount.getPassword()))
                    .authorities(adminAccount.getAccountRole().getRole().getAuthority())
                    .build();
        }

        return accountService.findByUsername(username)
                .map(account -> User.builder()
                        .username(account.getUsername())
                        .password(passwordEncoder.encode(account.getPassword()))
                        .authorities(account.getAccountRole().getRole().getAuthority())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public boolean hasAdminAccount() {
        return accountService.existsByAccountRole_Role(AccountRoleEnum.ADMIN);
    }
}
