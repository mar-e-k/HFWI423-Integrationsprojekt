package de.fhdw.fillialensystem.utility.security;

import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.entity.AccountRole;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.fillialensystem.persistence.service.AccountService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomUserDetails implements UserDetailsService {

    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetails(AccountService accountService, PasswordEncoder passwordEncoder) {
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if ("admin".equals(username) && !existsAdminAccount()) {
            return new Account(
                    new AccountRole(
                            null,
                            AccountRoleEnum.ADMIN),
                    UUID.randomUUID().toString(),
                    "admin",
                    passwordEncoder.encode("admin"));
        }

        return accountService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: [%s]".formatted(username)));
    }

    public boolean existsAdminAccount() {
        return accountService.findAll().stream().anyMatch(account -> account.getAccountRole().getRole().equals(AccountRoleEnum.ADMIN));
    }
}
