package de.fhdw.vendix.store.utility.security;

import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import de.fhdw.vendix.commons.core.persistence.entity.LockTypeEnum;
import de.fhdw.vendix.commons.security.auth.AuthContext;
import de.fhdw.vendix.store.persistence.entity.Account;
import de.fhdw.vendix.store.persistence.service.AccountService;
import de.fhdw.vendix.store.persistence.service.DistributedLockService;
import de.fhdw.vendix.store.utility.StoreClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final String API_TOKEN_PATH = "/api/auth/token";

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

        // API-Token-Requests (z.B. von JMeter) sollen den Concurrent-Session-Lock
        // nicht auslösen — nur Browser-Logins via /login werden gesperrt.
        boolean isApiTokenRequest = isApiTokenRequest();
        boolean locked = !isApiTokenRequest &&
                distributedLockService.existsByLockTypeAndTargetId(LockTypeEnum.ACCOUNT, account.getId());

        boolean allowed = account.getAccountRole().getRole() == AccountRoleEnum.ADMIN;

        return new AuthContext.Builder(account.getId(), account.getUuid(), account.getUsername(), account.getPassword(), Set.of(account.getAccountRole().getRole()))
                .storeId((storeClient.getStore() != null) && (storeClient.getStore().getId() != null)
                        ? storeClient.getStore().getId() : null)
                .accountNonLocked(!locked)
                .accountEnabled(allowed)
                .build();
    }

    /**
     * Gibt true zurück, wenn die aktuelle Anfrage ein REST-API-Token-Request ist
     * (POST /api/auth/token). In diesem Fall wird der Distributed-Lock-Check
     * übersprungen, damit JMeter und andere API-Clients sich mehrfach authentifizieren
     * können, ohne sich gegenseitig zu blockieren.
     */
    private boolean isApiTokenRequest() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                return API_TOKEN_PATH.equals(request.getRequestURI());
            }
        } catch (Exception ignored) {
            // Kein Request-Kontext vorhanden (z.B. beim Start) → kein API-Request
        }
        return false;
    }
}