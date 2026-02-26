package de.fhdw.vendix.store.utility.security;

import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import de.fhdw.vendix.commons.core.persistence.entity.LockTypeEnum;
import de.fhdw.vendix.store.persistence.entity.Account;
import de.fhdw.vendix.store.persistence.service.AccountService;
import de.fhdw.vendix.store.persistence.service.DistributedLockService;
import de.fhdw.vendix.store.utility.StoreClient;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountService accountService;
    private final DistributedLockService distributedLockService;
    private final StoreClient storeClient;

    public CustomUserDetailsService(AccountService accountService, DistributedLockService distributedLockService, StoreClient storeClient) {
        this.accountService = accountService;
        this.distributedLockService = distributedLockService;
        this.storeClient = storeClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean locked = distributedLockService.existsByLockTypeAndTargetId(LockTypeEnum.ACCOUNT, account.getId());

        boolean allowed = account.getAccountRole().getRole() == AccountRoleEnum.ADMIN;

        return new AuthContext.Builder(account.getId(), account.getUuid(), account.getUsername(), account.getPassword(), Set.of(account.getAccountRole().getRole()))
                .storeId((storeClient.getStore() != null) && (storeClient.getStore().getId() != null)
                        ? storeClient.getStore().getId() : null)
                .accountNonLocked(!locked)
                .accountEnabled(allowed)
                .build();
    }
}