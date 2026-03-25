package de.fhdw.vendix.pos.core.persistence;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.web.AccountCommandApi;
import de.fhdw.vendix.commons.api.domain.account.web.AccountEndpoints;
import de.fhdw.vendix.commons.api.domain.account.web.AccountQueryApi;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.spring.web.AbstractApiClient;
import de.fhdw.vendix.security.api.jwt.JwtService;
import de.fhdw.vendix.security.api.jwt.payload.JwtPayload;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
class AccountClientAdapter extends AbstractApiClient implements AccountQueryApi, AccountCommandApi {

    AccountClientAdapter(JwtService jwtService) {
        super(jwtService, "localhost:8080");
    }

    @Override
    public Optional<AccountDTO> findByUsername(String username) {
        return Optional.empty();
    }

    @Override
    public Optional<AccountDTO> findByUUID(UUID uuid) {
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

    @Override
    public Set<AccountRoleEnum> findAllRoles(Long id) {
        return Set.of();
    }

    @Override
    protected JwtPayload buildJwtPayload() {
        return JwtPayload.system();
    }


//    public Optional<AccountDTO> findById(Long id) {
//        return getWebClient()
//                .get()
//                .uri("/api/cashier/id/{id}", id)
//                .retrieve()
//                .bodyToMono(AccountDTO.class)
//                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
//                .blockOptional();
//    }
//
//    public Optional<AccountDTO> findByUuid(String uuid) {
//        return getWebClient()
//                .get()
//                .uri("/api/cashier/subject/{subject}", uuid)
//                .retrieve()
//                .bodyToMono(AccountDTO.class)
//                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
//                .blockOptional();
//    }
//
//    public Optional<AccountDTO> findByUsername(String username) {
//        return getWebClient()
//                .get()
//                .uri("/api/cashier/name/{accountUsername}", username)
//                .retrieve()
//                .bodyToMono(AccountDTO.class)
//                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
//                .blockOptional();
//    }
}