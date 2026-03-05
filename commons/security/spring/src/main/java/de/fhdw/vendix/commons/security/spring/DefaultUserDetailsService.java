package de.fhdw.vendix.commons.security.spring;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.domain.lock.port.LockQueryPort;
import de.fhdw.vendix.commons.security.core.DefaultAuthContext;
import de.fhdw.vendix.security.api.auth.AuthContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public final class DefaultUserDetailsService implements UserDetailsService {

    private final AccountQueryPort accountQueryPort;
    private final LockQueryPort lockQueryPort;

    public DefaultUserDetailsService(AccountQueryPort accountQueryPort, LockQueryPort lockQueryPort) {
        this.accountQueryPort = accountQueryPort;
        this.lockQueryPort = lockQueryPort;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountDTO accountDTO = accountQueryPort.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        boolean lockExists = lockQueryPort.existsByTargetTypeAndTargetID(TargetTypeEnum.ACCOUNT, accountDTO.id());

        AuthContext authContext = new DefaultAuthContext(
                accountDTO,
                true,
                lockExists,
                true,
                true
        );

        return new SpringUserDetailsAdapter(authContext);
    }
}