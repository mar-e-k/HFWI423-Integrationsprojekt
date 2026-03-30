package de.fhdw.vendix.store.core.specification;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.security.api.authentication.AuthenticationService;
import de.fhdw.vendix.store.core.persistance.account.port.AccountService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
class AuthenticationAdapter implements AuthenticationService {

    private final AccountService accountPort;

    AuthenticationAdapter(AccountService accountPort) {
        this.accountPort = accountPort;
    }

    @Override
    public Optional<AccountDTO> findByUsername(String username) {
        return accountPort.findByUsername(username);
    }

    @Override
    public Optional<AccountDTO> findByUUID(UUID uuid) {
        return accountPort.findByUUID(uuid);
    }

    @Override
    public Optional<AccountDTO> findByPhone(String phone) {
        return accountPort.findByPhone(phone);
    }

    @Override
    public Optional<AccountDTO> findByEmail(String email) {
        return accountPort.findByEmail(email);
    }
}