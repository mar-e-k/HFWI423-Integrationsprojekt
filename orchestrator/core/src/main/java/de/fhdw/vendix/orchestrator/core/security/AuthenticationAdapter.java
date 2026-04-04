package de.fhdw.vendix.orchestrator.core.security;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.spring.security.authentication.AuthenticationService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
class AuthenticationAdapter implements AuthenticationService {

    @Override
    public Optional<AccountDTO> findByUUID(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public Optional<AccountDTO> findByUsername(String username) {
        return Optional.empty();
    }

    @Override
    public Optional<AccountDTO> findByPhone(String phone) {
        return Optional.empty();
    }

    @Override
    public Optional<AccountDTO> findByEmail(String email) {
        return Optional.empty();
    }
}