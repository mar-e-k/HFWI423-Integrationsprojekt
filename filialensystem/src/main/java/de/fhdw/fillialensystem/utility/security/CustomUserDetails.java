package de.fhdw.fillialensystem.utility.security;

import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.entity.AccountRole;
import de.fhdw.fillialensystem.persistence.service.AccountService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetails implements UserDetailsService {

    private final AccountService accountService;

    public CustomUserDetails(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: [%s]".formatted(username)));
    }

    public boolean existsAtLeastOneAdminAccount() {
        return accountService.findAll().stream().map(Account::getAccountRole).map(AccountRole::getRole).anyMatch(role -> role == AccountRoleEnum.ADMIN);
    }

    public boolean existsAtLeastOneCashierAccount() {
        return accountService.findAll().stream().map(Account::getAccountRole).map(AccountRole::getRole).anyMatch(role -> role == AccountRoleEnum.ADMIN);
    }
}
