package de.fhdw.vendix.pos.core.specification;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.api.domain.account.web.AccountQueryApi;
import de.fhdw.vendix.security.api.authentication.AuthenticationCommandApi;
import de.fhdw.vendix.security.api.authentication.AuthenticationQueryApi;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
class AuthenticationAdapter implements AuthenticationQueryApi, AuthenticationCommandApi {

    private final AccountQueryApi accountQueryApi;

    AuthenticationAdapter(AccountQueryApi accountQueryApi) {
        this.accountQueryApi = accountQueryApi;
    }

    @Override
    public Optional<AccountDTO> findByUsername(String username) {
        return accountQueryApi.findByUsername(username);
    }

    @Override
    public Optional<AccountDTO> findByUUID(UUID uuid) {
        return accountQueryApi.findByUUID(uuid);
    }

    @Override
    public Optional<AccountDTO> findByPhone(String phone) {
        return accountQueryApi.findByPhone(phone);
    }

    @Override
    public Optional<AccountDTO> findByEmail(String email) {
        return accountQueryApi.findByEmail(email);
    }
}