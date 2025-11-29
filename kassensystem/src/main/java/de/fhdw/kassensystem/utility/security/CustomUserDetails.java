package de.fhdw.kassensystem.utility.security;

import de.fhdw.commons.api.dto.AccountDTO;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.kassensystem.persistance.service.proxy.AccountProxyService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetails implements UserDetailsService {

    private final AccountProxyService accountProxyService;

    public CustomUserDetails(AccountProxyService accountProxyService) {
        this.accountProxyService = accountProxyService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountProxyService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: [%s]".formatted(username)));
    }

    public boolean existsAtLeastOneAdminAccount() {
        return accountProxyService.findAll().stream().map(AccountDTO::getRole).anyMatch(role -> role == AccountRoleEnum.ADMIN);
    }

    public boolean existsAtLeastOneCashierAccount() {
        return accountProxyService.findAll().stream().map(AccountDTO::getRole).anyMatch(role -> role == AccountRoleEnum.ADMIN);
    }
}
