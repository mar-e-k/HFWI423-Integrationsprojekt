package de.fhdw.kassensystem.utility.security;

import de.fhdw.commons.api.dto.AccountDTO;
import de.fhdw.commons.utility.AuthContext;
import de.fhdw.kassensystem.persistance.service.proxy.AccountProxyService;
import de.fhdw.kassensystem.utility.RegisterClient;
import de.fhdw.kassensystem.utility.StoreClient;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountProxyService accountProxyService;
    private final StoreClient storeClient;
    private final RegisterClient registerClient;

    public CustomUserDetailsService(AccountProxyService accountProxyService, StoreClient storeClient, RegisterClient registerClient) {
        this.accountProxyService = accountProxyService;
        this.storeClient = storeClient;
        this.registerClient = registerClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountDTO dto = accountProxyService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new AuthContext(
                dto.getRole(),
                dto.getUuid(),
                dto.getUsername(),
                dto.getPassword(),
                (storeClient.getStoreDTO() != null) && (storeClient.getStoreDTO().getId() != null) ? storeClient.getStoreDTO().getId().intValue() : null,
                (registerClient.getRegisterDTO() != null) && (registerClient.getRegisterDTO().getId() != null) ? registerClient.getRegisterDTO().getId().intValue() : null
        );
    }
}
