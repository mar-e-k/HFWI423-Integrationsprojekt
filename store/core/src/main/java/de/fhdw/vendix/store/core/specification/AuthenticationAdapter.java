package de.fhdw.vendix.store.core.specification;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.security.api.authentication.AuthenticationCommandApi;
import de.fhdw.vendix.security.api.authentication.AuthenticationQueryApi;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
class AuthenticationAdapter implements AuthenticationQueryApi, AuthenticationCommandApi {

    private final AccountQueryPort accountQueryPort;

    public AuthenticationAdapter(AccountQueryPort accountQueryPort) {
        this.accountQueryPort = accountQueryPort;
    }

    @Override
    public Optional<AccountDTO> findByUsername(String username) {
        return accountQueryPort.findByUsername(username);
    }

    @Override
    public Optional<AccountDTO> findByUUID(UUID uuid) {
        return accountQueryPort.findByUUID(uuid);
    }

    @Override
    public Optional<AccountDTO> findByPhone(String phone) {
        return accountQueryPort.findByPhone(phone);
    }

    @Override
    public Optional<AccountDTO> findByEmail(String email) {
        return accountQueryPort.findByEmail(email);
    }
}