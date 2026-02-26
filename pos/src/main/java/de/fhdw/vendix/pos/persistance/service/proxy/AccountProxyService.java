package de.fhdw.vendix.pos.persistance.service.proxy;

import de.fhdw.vendix.commons.core.api.dto.AccountDTO;
import de.fhdw.vendix.pos.utility.StoreClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class AccountProxyService extends AbstractProxyService {

    public AccountProxyService(StoreClient storeClient) {
        super(storeClient);
    }

    public Optional<AccountDTO> findById(Long id) {
        return getWebClient()
                .get()
                .uri("/api/account/id/{id}", id)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .blockOptional();
    }

    public Optional<AccountDTO> findByUuid(String uuid) {
        return getWebClient()
                .get()
                .uri("/api/account/subject/{subject}", uuid)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .blockOptional();
    }

    public Optional<AccountDTO> findByUsername(String username) {
        return getWebClient()
                .get()
                .uri("/api/account/name/{accountUsername}", username)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .blockOptional();
    }
}