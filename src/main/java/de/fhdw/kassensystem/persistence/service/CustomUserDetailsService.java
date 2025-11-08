package de.fhdw.kassensystem.persistence.service;

import de.fhdw.kassensystem.persistence.entity.AccountRoleEnum;
import de.fhdw.kassensystem.persistence.repository.AccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByUsername(username)
                .map(account -> User.builder()
                        .username(account.getUsername())
                        .password(passwordEncoder.encode(account.getPassword()))
                        .authorities(account.getAccountRole().getRole().getAuthority())
                        .build())
                .orElseGet(() -> {
                    if ("admin".equals(username) && !hasAdminAccount()) {
                        return User.builder()
                                .username("admin")
                                .password(passwordEncoder.encode("admin"))
                                .authorities(AccountRoleEnum.ADMIN.getAuthority())
                                .build();
                    }
                    throw new UsernameNotFoundException("User not found: " + username);
                });
    }

    public boolean hasAdminAccount() {
        return accountRepository.existsByAccountRole_Role(AccountRoleEnum.ADMIN);
    }
}
