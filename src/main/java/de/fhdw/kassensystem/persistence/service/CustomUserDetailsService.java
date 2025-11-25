package de.fhdw.kassensystem.persistence.service;

import de.fhdw.kassensystem.persistence.entity.Account;
import de.fhdw.kassensystem.persistence.entity.AccountRole;
import de.fhdw.kassensystem.persistence.entity.AccountRoleEnum;
import de.fhdw.kassensystem.persistence.repository.AccountRepository;
import de.fhdw.kassensystem.persistence.repository.AccountRoleRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(AccountRepository accountRepository,
                                    AccountRoleRepository accountRoleRepository,
                                    PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if ("admin".equals(username) && !hasAdminAccount()) {

            // Rolle prüfen oder erstellen
            AccountRole adminRole = accountRoleRepository.findByRole(AccountRoleEnum.ADMIN)
                    .orElseGet(() -> accountRoleRepository.save(new AccountRole(null, AccountRoleEnum.ADMIN)));

            // Admin-Account erstellen
            Account adminAccount = new Account(adminRole, 0, "admin", "admin");
            accountRepository.save(adminAccount);

            return User.builder()
                    .username(adminAccount.getUsername())
                    .password(passwordEncoder.encode(adminAccount.getPassword()))
                    .authorities(adminAccount.getAccountRole().getRole().getAuthority())
                    .build();
        }

        return accountRepository.findByUsername(username)
                .map(account -> User.builder()
                        .username(account.getUsername())
                        .password(passwordEncoder.encode(account.getPassword()))
                        .authorities(account.getAccountRole().getRole().getAuthority())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public boolean hasAdminAccount() {
        return accountRepository.existsByAccountRole_Role(AccountRoleEnum.ADMIN);
    }
}
