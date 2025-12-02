package de.fhdw.fillialensystem.utility.security;

import de.fhdw.commons.utility.AuthContext;
import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.repository.AccountRepository;
import de.fhdw.fillialensystem.utility.StoreClient;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final StoreClient storeClient;

    public CustomUserDetailsService(AccountRepository accountRepository, StoreClient storeClient) {
        this.accountRepository = accountRepository;
        this.storeClient = storeClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new AuthContext(
                account.getAccountRole().getRole(),
                account.getUuid(),
                account.getUsername(),
                account.getPassword(),
                storeClient.getStore() != null ? storeClient.getStore().getId().intValue() : null,
                null
        );
    }
}