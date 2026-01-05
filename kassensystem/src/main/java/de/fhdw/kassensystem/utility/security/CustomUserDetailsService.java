package de.fhdw.kassensystem.utility.security;

import de.fhdw.commons.api.dto.AccountDTO;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.commons.persistence.entity.LockTypeEnum;
import de.fhdw.commons.security.utility.security.auth.AuthContext;
import de.fhdw.kassensystem.persistance.service.proxy.AccountProxyService;
import de.fhdw.kassensystem.persistance.service.proxy.DistributedLockProxyService;
import de.fhdw.kassensystem.utility.RegisterClient;
import de.fhdw.kassensystem.utility.StoreClient;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountProxyService accountProxyService;
    private final DistributedLockProxyService distributedLockProxyService;
    private final StoreClient storeClient;
    private final RegisterClient registerClient;

    public CustomUserDetailsService(AccountProxyService accountProxyService, DistributedLockProxyService distributedLockProxyService, StoreClient storeClient, RegisterClient registerClient) {
        this.accountProxyService = accountProxyService;
        this.distributedLockProxyService = distributedLockProxyService;
        this.storeClient = storeClient;
        this.registerClient = registerClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountDTO account = accountProxyService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean locked = distributedLockProxyService
                .existsByLockTypeAndTargetId(LockTypeEnum.ACCOUNT, account.getId())
                .blockOptional()
                .orElse(false);

        boolean allowed = account.getRole() == AccountRoleEnum.CASHIER;

        return new AuthContext.Builder(account)
                .storeId((storeClient.getStoreDTO() != null) && (storeClient.getStoreDTO().getId() != null)
                        ? storeClient.getStoreDTO().getId() : null)
                .registerId((registerClient.getRegister() != null) && (registerClient.getRegister().getId() != null)
                        ? registerClient.getRegister().getId() : null)
                .accountNonLocked(!locked)
                .accountEnabled(allowed)
                .build();
    }
}
